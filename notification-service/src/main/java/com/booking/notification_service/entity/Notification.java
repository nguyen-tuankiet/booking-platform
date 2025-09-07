package com.booking.notification_service.entity;

import com.booking.common_library.entity.BaseEntity;
import com.booking.notification_service.utils.NotificationChannel;
import com.booking.notification_service.utils.NotificationStatus;
import com.booking.notification_service.utils.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification extends BaseEntity {
    @Id
    private String id;
    private String userId;
    private NotificationType type; // EMAIL, SMS, PUSH
    private NotificationChannel channel; // BOOKING, PAYMENT, PROMOTION
    private String recipient;
    private String subject;
    private String content;
    private String templateName;
    private Map<String, Object> templateData;
    private NotificationStatus status; // PENDING, SENT, FAILED, RETRY
    private String errorMessage;
    private int retryCount;
    private int maxRetries;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Integer priority; // 1-10, higher = more urgent
}