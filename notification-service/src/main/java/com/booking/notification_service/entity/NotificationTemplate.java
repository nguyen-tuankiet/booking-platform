package com.booking.notification_service.entity;

import com.booking.common_library.entity.BaseEntity;
import com.booking.notification_service.utils.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate extends BaseEntity {
    @Id
    private String id;
    private String name;
    private NotificationType type; // EMAIL, SMS, PUSH
    private String subject;
    private String content;
    private String language;
    private boolean active;
}