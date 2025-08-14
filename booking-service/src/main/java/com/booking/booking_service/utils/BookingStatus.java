package com.booking.booking_service.utils;

public enum BookingStatus {
    PENDING,    // Chờ xác nhận
    LOCKED,     // Đã giữ ghế
    CONFIRMED,  // Đã xác nhận đặt vé
    CANCELLED,  // Đã hủy
    COMPLETED,  // Hoàn tất chuyến đi
    EXPIRED     // Hết hạn giữ ghế
}
