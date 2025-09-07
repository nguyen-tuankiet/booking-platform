package com.booking.notification_service.dto.respone;

import com.booking.notification_service.utils.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private NotificationStatus status;
    private String message;
}
