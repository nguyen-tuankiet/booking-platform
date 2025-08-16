package com.booking.payment_service.service;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.PaymentResponse;
import com.booking.payment_service.dto.respone.PaymentCallbackRequest;

public interface PaymentGatewayAdapter {
    PaymentResponse createPayment(PaymentRequest request, String transactionId);
    PaymentResponse processCallback(PaymentCallbackRequest callback);
    boolean verifySignature(PaymentCallbackRequest callback);
    String getGatewayName();
}
