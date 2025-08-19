package com.booking.payment_service.utils;

public enum TransactionStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIAL_REFUND,
    COMPLETED
}
