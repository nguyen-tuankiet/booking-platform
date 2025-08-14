package com.booking.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    private String email; // Email liên hệ
    private String phoneNumber; // Số điện thoại liên hệ
    private String emergencyContact; // Tên người liên hệ khẩn cấp
    private String emergencyPhone; // Số điện thoại khẩn cấp
}
