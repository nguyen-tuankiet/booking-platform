package com.booking.payment_service.service.impl;

import com.booking.common_library.exception.BusinessException;
import com.booking.common_library.exception.ResourceNotFoundException;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.RefundResponse;
import com.booking.payment_service.entity.Refund;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.repository.RefundRepository;
import com.booking.payment_service.service.RefundService;
import com.booking.payment_service.service.TransactionService;
import com.booking.payment_service.utils.RefundStatus;
import com.booking.payment_service.utils.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final TransactionService transactionService;

    @Override
    public RefundResponse processRefund(RefundRequest request) {
        try {
            // Get original transaction
            Transaction transaction = transactionService.getTransactionByIdOrThrow(request.getTransactionId());

            // Validate refund eligibility
            validateRefundEligibility(transaction, request.getAmount());

            // Create refund record
            Refund refund = createRefundRecord(transaction, request);

            // Process refund through payment gateway
            processGatewayRefund(refund);

            return mapToRefundResponse(refund);

        } catch (Exception e) {
            log.error("Refund processing failed for transaction: {}", request.getTransactionId(), e);
            throw new BusinessException("Refund processing failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundStatus(String refundId) {
        Refund refund = refundRepository.findByRefundIdAndDeleted(refundId, false)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found: " + refundId));

        return mapToRefundResponse(refund);
    }

    private void validateRefundEligibility(Transaction transaction, BigDecimal refundAmount) {
        // Check if transaction is successful
        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            throw new BusinessException("Cannot refund unsuccessful transaction");
        }

        // Check refund amount
        BigDecimal totalRefunded = refundRepository.getTotalRefundedAmount(transaction);
        if (totalRefunded == null) {
            totalRefunded = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = transaction.getAmount().subtract(totalRefunded);
        if (refundAmount.compareTo(remainingAmount) > 0) {
            throw new BusinessException("Refund amount exceeds remaining refundable amount");
        }
    }

    private Refund createRefundRecord(Transaction transaction, RefundRequest request) {
        String refundId = generateRefundId();

        Refund refund = Refund.builder()
                .refundId(refundId)
                .transaction(transaction)
                .amount(request.getAmount())
                .status(RefundStatus.PENDING)
                .reason(request.getReason())
                .build();

        return refundRepository.save(refund);
    }

    private void processGatewayRefund(Refund refund) {
        try {
            // In a real implementation, you would call the payment gateway's refund API
            // For demo purposes, we'll simulate the process

            String gatewayRefundId = "REF" + System.currentTimeMillis();

            // Simulate processing time
            Thread.sleep(1000);

            // Update refund status
            refund.setStatus(RefundStatus.SUCCESS);
            refund.setGatewayRefundId(gatewayRefundId);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setUpdatedAt(LocalDateTime.now());

            refundRepository.save(refund);

            // Update original transaction status
            updateTransactionRefundStatus(refund.getTransaction(), refund.getAmount());

            log.info("Refund processed successfully: {}", refund.getRefundId());

        } catch (Exception e) {
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailedReason(e.getMessage());
            refund.setUpdatedAt(LocalDateTime.now());
            refundRepository.save(refund);

            log.error("Gateway refund processing failed for: {}", refund.getRefundId(), e);
            throw new BusinessException("Gateway refund processing failed", e);
        }
    }

    private void updateTransactionRefundStatus(Transaction transaction, BigDecimal refundAmount) {
        BigDecimal totalRefunded = refundRepository.getTotalRefundedAmount(transaction);
        if (totalRefunded == null) {
            totalRefunded = BigDecimal.ZERO;
        }

        if (totalRefunded.compareTo(transaction.getAmount()) == 0) {
            // Full refund
            transactionService.updateTransactionStatus(
                    transaction.getTransactionId(),
                    TransactionStatus.REFUNDED,
                    null
            );
        } else {
            // Partial refund
            transactionService.updateTransactionStatus(
                    transaction.getTransactionId(),
                    TransactionStatus.PARTIAL_REFUND,
                    null
            );
        }
    }

    private String generateRefundId() {
        return "RFD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private RefundResponse mapToRefundResponse(Refund refund) {
        return RefundResponse.builder()
                .refundId(refund.getRefundId())
                .transactionId(refund.getTransaction().getTransactionId())
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
