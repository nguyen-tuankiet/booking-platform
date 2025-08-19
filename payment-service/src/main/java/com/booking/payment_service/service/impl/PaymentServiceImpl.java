// ===== Updated Payment Service Implementation =====

package com.booking.payment_service.service.impl;

import com.booking.common_library.entity.payment_event.*;
import com.booking.payment_service.dto.request.OTPVerificationRequest;
import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.*;
import com.booking.payment_service.entity.PaymentSession;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.saga.BookingPaymentSagaOrchestrator;
import com.booking.payment_service.service.*;
import com.booking.payment_service.utils.PaymentSessionStatus;
import com.booking.payment_service.utils.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayService gatewayService;
    private final PaymentSessionService sessionService;
    private final TransactionService transactionService;
    private final OTPService otpService;
    private final RefundService refundService;
    private final PaymentEventPublisher eventPublisher;
    private final BookingPaymentSagaOrchestrator sagaOrchestrator;

    @Override
    public List<PaymentGatewayResponse> getAvailableGateways(BigDecimal amount, String currency) {
        log.info("Getting available gateways for amount: {} {}", amount, currency);
        return gatewayService.getAvailableGateways(amount, currency);
    }

    @Override
    @Transactional
    public PaymentSessionResponse createPaymentSession(PaymentRequest request) {
        log.info("Creating payment session for booking: {}", request.getBookingId());

        PaymentSession session = sessionService.createPaymentSession(request);

        return PaymentSessionResponse.builder()
                .sessionId(session.getSessionId())
                .bookingId(session.getBookingId())
                .amount(session.getAmount())
                .currency(session.getCurrency())
                .status(PaymentSessionStatus.valueOf(session.getStatus().toString()))
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();
    }

    @Override
    public PaymentSessionResponse getPaymentSession(String sessionId) {
        log.info("Getting payment session: {}", sessionId);

        PaymentSession session = sessionService.getSessionById(sessionId);

        return PaymentSessionResponse.builder()
                .sessionId(session.getSessionId())
                .bookingId(session.getBookingId())
                .amount(session.getAmount())
                .currency(session.getCurrency())
                .status(PaymentSessionStatus.valueOf(session.getStatus().toString()))
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for booking: {} with amount: {} {}",
                request.getBookingId(), request.getAmount(), request.getCurrency());

        try {
            // Create payment through gateway (this will also create the transaction)
            PaymentResponse gatewayResponse = gatewayService.createPayment(request);

            // Start Saga orchestration with the created transaction
            sagaOrchestrator.startBookingPaymentSaga(
                    String.valueOf(request.getBookingId()),
                    gatewayResponse.getTransactionId(),
                    request.getUserId()
            );

            // Publish payment initiated event
            PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                    .transactionId(gatewayResponse.getTransactionId())
                    .bookingId(String.valueOf(gatewayResponse.getBookingId()))
                    .userId(request.getUserId())
                    .gateway(gatewayResponse.getPaymentGateway())
                    .amount(gatewayResponse.getAmount())
                    .currency(gatewayResponse.getCurrency())
                    .paymentUrl(gatewayResponse.getPaymentUrl())
                    .initiatedAt(LocalDateTime.now())
                    .sessionId(UUID.randomUUID().toString())
                    .build();

            eventPublisher.publishPaymentInitiated(event);

            // Check if OTP is required for high-value transactions
            if (isOTPRequired(request)) {
                // Retrieve the created transaction for OTP handling
                Transaction transaction = transactionService.getTransactionByIdOrThrow(gatewayResponse.getTransactionId());
                handleOTPRequired(transaction, request);
            }

            log.info("Payment initiated successfully for booking: {}, transaction: {}",
                    request.getBookingId(), gatewayResponse.getTransactionId());

            return gatewayResponse;

        } catch (Exception e) {
            log.error("Failed to process payment for booking: {}", request.getBookingId(), e);

            // Publish payment failed event
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .bookingId(String.valueOf(request.getBookingId()))
                    .userId(request.getUserId())
                    .gateway(String.valueOf(request.getPaymentMethod()))
                    .reason(e.getMessage())
                    .failedAt(LocalDateTime.now())
                    .retryable(true)
                    .build();

            eventPublisher.publishPaymentFailed(failedEvent);
            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse handleCallback(String gateway, PaymentCallbackRequest callback) {
        log.info("Handling payment callback from gateway: {} for transaction: {}",
                gateway, callback.getTransactionId());

        try {
            // Verify signature
            boolean isValid = gatewayService.verifySignature(gateway, callback);
            if (!isValid) {
                log.error("Invalid signature for callback from gateway: {}", gateway);
                throw new RuntimeException("Invalid payment callback signature");
            }

            // Process callback via gateway adapter (this updates transaction status internally)
            PaymentResponse response = gatewayService.processCallback(gateway, callback);

            // Get updated transaction
            Transaction transaction = transactionService.getTransactionByIdOrThrow(callback.getTransactionId());

            if (response.getStatus() == TransactionStatus.SUCCESS) {
                // Publish payment completed event
                PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                        .transactionId(transaction.getTransactionId())
                        .bookingId(String.valueOf(transaction.getBookingId()))
                        .userId(transaction.getUserId())
                        .gateway(gateway)
                        .gatewayTransactionId(transaction.getGatewayTransactionId())
                        .amount(transaction.getAmount())
                        .currency(transaction.getCurrency())
                        .status(response.getStatus().name())
                        .completedAt(LocalDateTime.now())
                        .paymentMethod(String.valueOf(transaction.getPaymentMethod()))
                        .build();

                eventPublisher.publishPaymentCompleted(completedEvent);

                // Notify saga orchestrator
                sagaOrchestrator.handlePaymentCallback(callback.getTransactionId(), true, null);
            } else {
                // Publish payment failed event
                PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                        .transactionId(transaction.getTransactionId())
                        .bookingId(String.valueOf(transaction.getBookingId()))
                        .userId(transaction.getUserId())
                        .gateway(gateway)
                        .reason("Gateway callback failed with status: " + callback.getStatus())
                        .failedAt(LocalDateTime.now())
                        .retryable(false)
                        .build();

                eventPublisher.publishPaymentFailed(failedEvent);

                // Notify saga orchestrator
                sagaOrchestrator.handlePaymentCallback(
                        callback.getTransactionId(),
                        false,
                        "Gateway callback failed with status: " + callback.getStatus()
                );
            }

            return response;

        } catch (Exception e) {
            log.error("Error handling payment callback for transaction: {}", callback.getTransactionId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyOTP(OTPVerificationRequest request) {
        log.info("Verifying OTP for transaction: {}", request.getTransactionId());

        try {
            // Verify OTP
            boolean isValid = otpService.verifyOTP(request.getTransactionId(), request.getOtpCode());

            if (!isValid) {
                log.warn("Invalid OTP provided for transaction: {}", request.getTransactionId());
                throw new RuntimeException("Invalid OTP code");
            }

            // Get transaction
            Transaction transaction = transactionService.getTransactionByIdOrThrow(request.getTransactionId());

            // Update transaction status
            transactionService.updateTransactionStatus(
                    request.getTransactionId(),
                    TransactionStatus.SUCCESS,
                    "OTP-" + request.getTransactionId()
            );

            // Clear OTP
            otpService.clearOTP(request.getTransactionId());

            // Publish payment completed event
            PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                    .transactionId(request.getTransactionId())
                    .bookingId(String.valueOf(transaction.getBookingId()))
                    .userId(transaction.getUserId())
                    .gateway(transaction.getPaymentGateway())
                    .gatewayTransactionId("OTP-" + request.getTransactionId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .status(TransactionStatus.SUCCESS.name())
                    .completedAt(LocalDateTime.now())
                    .paymentMethod(String.valueOf(transaction.getPaymentMethod()))
                    .build();

            eventPublisher.publishPaymentCompleted(completedEvent);

            log.info("OTP verification successful for transaction: {}", request.getTransactionId());

            return PaymentResponse.builder()
                    .transactionId(transaction.getTransactionId())
                    .bookingId(transaction.getBookingId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .status(TransactionStatus.SUCCESS)
                    .paymentMethod(transaction.getPaymentMethod())
                    .paymentGateway(transaction.getPaymentGateway())
                    .processedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("OTP verification failed for transaction: {}", request.getTransactionId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund for transaction: {}", request.getTransactionId());

        try {
            RefundResponse refund = refundService.processRefund(request);

            // Get original transaction
            Transaction originalTransaction = transactionService.getTransactionByIdOrThrow(
                    request.getTransactionId()
            );

            // Publish refund initiated event
            RefundInitiatedEvent refundEvent = RefundInitiatedEvent.builder()
                    .refundId(refund.getRefundId())
                    .originalTransactionId(request.getTransactionId())
                    .bookingId(String.valueOf(originalTransaction.getBookingId()))
                    .userId(originalTransaction.getUserId())
                    .refundAmount(refund.getAmount())
                    .reason(request.getReason())
                    .initiatedAt(LocalDateTime.now())
                    .build();

            eventPublisher.publishRefundInitiated(refundEvent);

            return refund;

        } catch (Exception e) {
            log.error("Failed to process refund for transaction: {}", request.getTransactionId(), e);
            throw e;
        }
    }

    @Override
    public Page<TransactionHistoryResponse> getTransactionHistory(Long userId, Pageable pageable) {
        log.info("Getting transaction history for user: {}", userId);
        return transactionService.getTransactionHistory(userId, pageable);
    }

    private boolean isOTPRequired(PaymentRequest request) {
        // OTP required for transactions > 5,000,000 VND
        return request.getAmount().compareTo(new BigDecimal("5000000")) > 0
                && "VND".equals(request.getCurrency());
    }

    private void handleOTPRequired(Transaction transaction, PaymentRequest request) {
        log.info("OTP required for high-value transaction: {}", transaction.getTransactionId());

        // Generate and send OTP
        otpService.generateAndSendOTP(transaction.getTransactionId());

        // Calculate priority based on amount (higher amount = higher priority)
        int priority = calculateOTPPriority(request.getAmount());

        // Publish OTP required event
        OTPRequiredEvent otpEvent = OTPRequiredEvent.builder()
                .transactionId(transaction.getTransactionId())
                .bookingId(String.valueOf(request.getBookingId()))
                .userId(request.getUserId())
                .phoneNumber("0123456789") // TODO: Get from user profile
                .email("user@example.com") // TODO: Get from user profile
                .requestedAt(LocalDateTime.now())
                .priority(priority)
                .build();

        eventPublisher.publishOTPRequired(otpEvent);
    }

    private int calculateOTPPriority(BigDecimal amount) {
        // Priority 1-10, higher amount = higher priority
        if (amount.compareTo(new BigDecimal("50000000")) > 0) return 10; // > 50M VND
        if (amount.compareTo(new BigDecimal("20000000")) > 0) return 8;  // > 20M VND
        if (amount.compareTo(new BigDecimal("10000000")) > 0) return 6;  // > 10M VND
        if (amount.compareTo(new BigDecimal("5000000")) > 0) return 4;   // > 5M VND
        return 2; // Default priority
    }
}