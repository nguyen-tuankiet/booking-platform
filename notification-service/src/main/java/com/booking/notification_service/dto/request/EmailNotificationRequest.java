package com.booking.notification_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest {
    private String userId;
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateData;
    private Integer priority;
}
