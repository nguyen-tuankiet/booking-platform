package com.booking.payment_service.service.impl;

import com.booking.common_library.exception.ResourceNotFoundException;
import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.TransactionHistoryResponse;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.repository.TransactionRepository;
import com.booking.payment_service.service.TransactionService;
import com.booking.payment_service.utils.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Transaction createTransaction(PaymentRequest request, String gateway) {
        String transactionId = generateTransactionId();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .bookingId(request.getBookingId())
                .userId(getCurrentUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .paymentGateway(gateway)
                .description(request.getDescription())
                .isPriority(request.getIsPriority())
                .retryCount(0)
                .otpVerified(false)
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction getTransactionByIdOrThrow(String transactionId) {
        return transactionRepository.findByTransactionIdAndDeleted(transactionId, false)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    @Override
    public void updateTransactionStatus(String transactionId, TransactionStatus status, String gatewayTransactionId) {
        Transaction transaction = getTransactionByIdOrThrow(transactionId);

        transaction.setStatus(status);
        transaction.setGatewayTransactionId(gatewayTransactionId);
        transaction.setProcessedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        log.info("Transaction {} status updated to {}", transactionId, status);
    }

    @Override
    public void updateTransactionWithGatewayResponse(String transactionId, String paymentUrl, String gatewayResponse) {
        Transaction transaction = getTransactionByIdOrThrow(transactionId);

        transaction.setGatewayResponse(gatewayResponse);
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        log.info("Transaction {} updated with gateway response", transactionId);
    }

    @Override
    public void markTransactionFailed(String transactionId, String reason) {
        Transaction transaction = getTransactionByIdOrThrow(transactionId);

        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailedReason(reason);
        transaction.setProcessedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        log.warn("Transaction {} marked as failed: {}", transactionId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionHistoryResponse> getTransactionHistory(Long userId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByUserIdAndDeleted(userId, false, pageable);

        return transactions.map(this::mapToHistoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getRetryableTransactions() {
        return transactionRepository.findRetryableTransactions(
                TransactionStatus.FAILED,
                LocalDateTime.now(),
                3 // Max retries
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getPriorityTransactions() {
        return transactionRepository.findPriorityTransactions(TransactionStatus.PENDING);
    }

    @Override
    public void processRetryTransaction(Transaction transaction) {
        if (transaction.getRetryCount() >= 3) {
            log.warn("Transaction {} exceeded maximum retry count", transaction.getTransactionId());
            return;
        }

        // Calculate next retry time (exponential backoff)
        LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(
                (long) Math.pow(2, transaction.getRetryCount())
        );

        transactionRepository.incrementRetryCount(
                transaction.getId(),
                nextRetryAt,
                LocalDateTime.now()
        );

        // Reset status to pending for retry
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setFailedReason(null);
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);

        log.info("Transaction {} scheduled for retry. Attempt: {}",
                transaction.getTransactionId(), transaction.getRetryCount() + 1);
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long getCurrentUserId() {
        // In a real application, this would come from SecurityContextHolder
        // For now, return a dummy value
        return 1L;
    }

    private TransactionHistoryResponse mapToHistoryResponse(Transaction transaction) {
        return TransactionHistoryResponse.builder()
                .transactionId(transaction.getTransactionId())
                .bookingId(transaction.getBookingId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .paymentGateway(transaction.getPaymentGateway())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .processedAt(transaction.getProcessedAt())
                .failedReason(transaction.getFailedReason())
                .build();
    }
}
