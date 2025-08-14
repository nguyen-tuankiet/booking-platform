package com.booking.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerInfo {
    private String title; // MR, MS, MRS
    private String firstName; // Tên
    private String lastName; // Họ
    private String dateOfBirth; // Ngày sinh
    private String nationality; // Quốc tịch
    private String passportNumber; // Số hộ chiếu
    private String passportExpiry; // Ngày hết hạn hộ chiếu
    private String seatNumber; // Ghế được gán
    private String meal; // Suất ăn: VEGETARIAN, HALAL, NORMAL
}