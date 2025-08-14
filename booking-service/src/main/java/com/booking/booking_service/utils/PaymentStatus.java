package com.booking.booking_service.utils;

public enum PaymentStatus {
    PENDING,           // Chờ thanh toán
    PROCESSING,        // Đang xử lý
    COMPLETED,         // Đã thanh toán
    FAILED,            // Thanh toán thất bại
    REFUNDED,          // Hoàn tiền toàn bộ
    PARTIALLY_REFUNDED // Hoàn tiền một phần
}
