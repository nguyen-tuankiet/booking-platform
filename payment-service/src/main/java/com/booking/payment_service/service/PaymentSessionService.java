package com.booking.payment_service.service;

import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.entity.PaymentSession;

public interface PaymentSessionService {
    PaymentSession createPaymentSession(PaymentRequest request);
    PaymentSession getSessionById(String sessionId);
    void expireOldSessions();

}
