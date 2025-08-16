package com.booking.payment_service.service;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.TransactionHistoryResponse;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.utils.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    Transaction createTransaction(PaymentRequest request, String gateway);
    Transaction getTransactionByIdOrThrow(String transactionId);
    void updateTransactionStatus(String transactionId, TransactionStatus status, String gatewayTransactionId);
    void updateTransactionWithGatewayResponse(String transactionId, String paymentUrl, String gatewayResponse);
    void markTransactionFailed(String transactionId, String reason);
    Page<TransactionHistoryResponse> getTransactionHistory(Long userId, Pageable pageable);
    List<Transaction> getRetryableTransactions();
    List<Transaction> getPriorityTransactions();
    void processRetryTransaction(Transaction transaction);
}
