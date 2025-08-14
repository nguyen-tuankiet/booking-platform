package com.booking.booking_service.utils;

public enum SeatStatus {
    AVAILABLE,    // Ghế trống
    OCCUPIED,     // Đã được đặt
    LOCKED,       // Đang được lock bởi user khác
    SELECTED,     // Đã chọn bởi user hiện tại
    UNAVAILABLE   // Không khả dụng
}
