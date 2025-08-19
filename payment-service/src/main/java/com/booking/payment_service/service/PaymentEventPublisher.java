package com.booking.payment_service.service;

import com.booking.common_library.entity.payment_event.*;

public interface PaymentEventPublisher {
    void publishPaymentInitiated(PaymentInitiatedEvent event);
    void publishPaymentCompleted(PaymentCompletedEvent event);
    void publishPaymentFailed(PaymentFailedEvent event);
    void publishOTPRequired(OTPRequiredEvent event);
    void publishRefundInitiated(RefundInitiatedEvent event);
    void publishRefundCompleted(RefundCompletedEvent event);
}
