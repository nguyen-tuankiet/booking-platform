package com.booking.payment_service.service;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.respone.PaymentCallbackRequest;
import com.booking.payment_service.dto.respone.PaymentGatewayResponse;
import com.booking.payment_service.dto.respone.PaymentResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentGatewayService {
    List<PaymentGatewayResponse> getAvailableGateways(BigDecimal amount, String currency);
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse processCallback(String gateway, PaymentCallbackRequest callback);
    boolean verifySignature(String gateway, PaymentCallbackRequest callback);
}
