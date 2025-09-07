package com.booking.notification_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationRequest {
    private String userId;
    private String phoneNumber;
    private String message;
    private String templateName;
    private Integer priority;
}