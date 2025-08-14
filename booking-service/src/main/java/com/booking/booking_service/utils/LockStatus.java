package com.booking.booking_service.utils;

public enum LockStatus {
    ACTIVE,    // Đang được lock
    EXPIRED,   // Đã hết hạn
    RELEASED,  // Đã được giải phóng
    CONFIRMED  // Đã xác nhận booking
}
