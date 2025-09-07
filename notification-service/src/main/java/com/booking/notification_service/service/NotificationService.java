package com.booking.notification_service.service;

import com.booking.notification_service.dto.request.EmailNotificationRequest;
import com.booking.notification_service.dto.request.SmsNotificationRequest;
import com.booking.notification_service.dto.respone.NotificationResponse;
import com.booking.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse sendEmail(EmailNotificationRequest request);
    NotificationResponse sendSms(SmsNotificationRequest request);
    Page<Notification> getUserNotifications(String userId, Pageable pageable);
    void processFailedNotifications();
}
