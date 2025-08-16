package com.booking.payment_service.service;


import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.request.OTPVerificationRequest;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    List<PaymentGatewayResponse> getAvailableGateways(BigDecimal amount, String currency);
    PaymentSessionResponse createPaymentSession(PaymentRequest request);
    PaymentSessionResponse getPaymentSession(String sessionId);
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse handleCallback(String gateway, PaymentCallbackRequest callback);
    PaymentResponse verifyOTP(OTPVerificationRequest request);
    RefundResponse processRefund(RefundRequest request);
    Page<TransactionHistoryResponse> getTransactionHistory(Long userId, Pageable pageable);
}