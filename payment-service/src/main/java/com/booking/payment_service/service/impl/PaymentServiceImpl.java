package com.booking.payment_service.service.impl;

import com.booking.common_library.exception.BusinessException;
import com.booking.common_library.exception.ResourceNotFoundException;
import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.request.OTPVerificationRequest;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.*;
import com.booking.payment_service.entity.PaymentSession;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.service.PaymentService;
import com.booking.payment_service.service.PaymentGatewayService;
import com.booking.payment_service.service.PaymentSessionService;
import com.booking.payment_service.service.TransactionService;
import com.booking.payment_service.service.RefundService;
import com.booking.payment_service.service.OTPService;
import com.booking.payment_service.utils.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentGatewayService gatewayService;
    private final PaymentSessionService sessionService;
    private final TransactionService transactionService;
    private final RefundService refundService;
    private final OTPService otpService;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentGatewayResponse> getAvailableGateways(BigDecimal amount, String currency) {
        return gatewayService.getAvailableGateways(amount, currency);
    }

    @Override
    public PaymentSessionResponse createPaymentSession(PaymentRequest request) {
        // Create payment session
        PaymentSession session = sessionService.createPaymentSession(request);

        return PaymentSessionResponse.builder()
                .sessionId(session.getSessionId())
                .bookingId(session.getBookingId())
                .amount(session.getAmount())
                .currency(session.getCurrency())
                .status(session.getStatus())
                .paymentGateway(session.getPaymentGateway())
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSessionResponse getPaymentSession(String sessionId) {
        PaymentSession session = sessionService.getSessionById(sessionId);
        return PaymentSessionResponse.builder()
                .sessionId(session.getSessionId())
                .bookingId(session.getBookingId())
                .amount(session.getAmount())
                .currency(session.getCurrency())
                .status(session.getStatus())
                .paymentGateway(session.getPaymentGateway())
                .paymentUrl(session.getPaymentUrl())
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // Check if OTP verification is required for priority payments
            if (request.getIsPriority() && !isOTPVerified(request)) {
                // Generate and send OTP
                otpService.generateAndSendOTP(request.getBookingId().toString());
                throw new BusinessException("OTP verification required for priority payment");
            }

            // Process payment through gateway
            PaymentResponse response = gatewayService.createPayment(request);

            log.info("Payment processed successfully for booking: {}", request.getBookingId());
            return response;

        } catch (Exception e) {
            log.error("Payment processing failed for booking: {}", request.getBookingId(), e);
            throw new BusinessException("Payment processing failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponse handleCallback(String gateway, PaymentCallbackRequest callback) {
        try {
            // Verify signature
            if (!gatewayService.verifySignature(gateway, callback)) {
                log.warn("Invalid signature for callback from gateway: {}", gateway);
                throw new BusinessException("Invalid payment callback signature");
            }

            // Process callback
            PaymentResponse response = gatewayService.processCallback(gateway, callback);

            // Send notification if payment successful
            if (response.getStatus() == TransactionStatus.SUCCESS) {
                // TODO: Send notification to user
                log.info("Payment successful for transaction: {}", response.getTransactionId());
            }

            return response;

        } catch (Exception e) {
            log.error("Callback processing failed for gateway: {}", gateway, e);
            throw new BusinessException("Callback processing failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponse verifyOTP(OTPVerificationRequest request) {
        try {
            // Verify OTP
            boolean isValid = otpService.verifyOTP(request.getTransactionId(), request.getOtpCode());

            if (!isValid) {
                throw new BusinessException("Invalid or expired OTP");
            }

            // Mark transaction as OTP verified
            Transaction transaction = transactionService.getTransactionByIdOrThrow(request.getTransactionId());
            transaction.setOtpVerified(true);

            return PaymentResponse.builder()
                    .transactionId(transaction.getTransactionId())
                    .bookingId(transaction.getBookingId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .status(transaction.getStatus())
                    .paymentMethod(transaction.getPaymentMethod())
                    .paymentGateway(transaction.getPaymentGateway())
                    .build();

        } catch (Exception e) {
            log.error("OTP verification failed for transaction: {}", request.getTransactionId(), e);
            throw new BusinessException("OTP verification failed: " + e.getMessage());
        }
    }

    @Override
    public RefundResponse processRefund(RefundRequest request) {
        return refundService.processRefund(request);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getTransactionHistory(Long userId, Pageable pageable) {
        return transactionService.getTransactionHistory(userId, pageable);
    }

    private boolean isOTPVerified(PaymentRequest request) {
        // Check if there's an existing transaction with OTP verified
        // This is a simplified check - in reality you might need more complex logic
        return false; // For now, always require OTP for priority payments
    }
}