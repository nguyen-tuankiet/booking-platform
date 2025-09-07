package com.booking.notification_service.service.Impl;

import com.booking.notification_service.dto.request.EmailNotificationRequest;
import com.booking.notification_service.dto.request.SmsNotificationRequest;
import com.booking.notification_service.dto.respone.NotificationResponse;
import com.booking.notification_service.entity.Notification;
import com.booking.notification_service.repository.NotificationRepository;
import com.booking.notification_service.service.*;
import com.booking.notification_service.utils.NotificationChannel;
import com.booking.notification_service.utils.NotificationStatus;
import com.booking.notification_service.utils.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final TemplateService templateService;
    private final RateLimitService rateLimitService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public NotificationResponse sendEmail(EmailNotificationRequest request) {
        // Check rate limit
        if (!rateLimitService.isAllowed("email", request.getUserId())) {
            return NotificationResponse.builder()
                    .status(NotificationStatus.RATE_LIMITED)
                    .message("Rate limit exceeded for email notifications")
                    .build();
        }

        try {
            // Create notification record
            Notification notification = Notification.builder()
                    .userId(request.getUserId())
                    .type(NotificationType.EMAIL)
                    .channel(NotificationChannel.MANUAL)
                    .recipient(request.getTo())
                    .templateName(request.getTemplateName())
                    .templateData(request.getTemplateData())
                    .status(NotificationStatus.PENDING)
                    .priority(request.getPriority() != null ? request.getPriority() : 5)
                    .maxRetries(3)
                    .retryCount(0)
                    .scheduledAt(LocalDateTime.now())
                    .build();

            // Process template
            if (request.getTemplateName() != null) {
                notification.setSubject(templateService.processSubject(
                        request.getTemplateName(), "EMAIL", request.getTemplateData()));
                notification.setContent(templateService.processTemplate(
                        request.getTemplateName(), "EMAIL", request.getTemplateData()));
            } else {
                notification.setSubject(request.getSubject());
                notification.setContent("Direct email content");
            }

            // Save notification
            notification = notificationRepository.save(notification);

            // Send email
            emailService.sendSimpleEmail(
                    notification.getRecipient(),
                    notification.getSubject(),
                    notification.getContent()
            );

            // Update status
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            return NotificationResponse.builder()
                    .id(notification.getId())
                    .status(NotificationStatus.SENT)
                    .message("Email sent successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            return NotificationResponse.builder()
                    .status(NotificationStatus.FAILED)
                    .message("Failed to send email: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public NotificationResponse sendSms(SmsNotificationRequest request) {
        // Check rate limit
        if (!rateLimitService.isAllowed("sms", request.getUserId())) {
            return NotificationResponse.builder()
                    .status(NotificationStatus.RATE_LIMITED)
                    .message("Rate limit exceeded for SMS notifications")
                    .build();
        }

        try {
            // Create notification record
            Notification notification = Notification.builder()
                    .userId(request.getUserId())
                    .type(NotificationType.SMS)
                    .channel(NotificationChannel.MANUAL)
                    .recipient(request.getPhoneNumber())
                    .templateName(request.getTemplateName())
                    .status(NotificationStatus.PENDING)
                    .priority(request.getPriority() != null ? request.getPriority() : 5)
                    .maxRetries(3)
                    .retryCount(0)
                    .scheduledAt(LocalDateTime.now())
                    .build();

            // Process message
            if (request.getTemplateName() != null) {
                notification.setContent(templateService.processTemplate(
                        request.getTemplateName(), "SMS", null));
            } else {
                notification.setContent(request.getMessage());
            }

            // Save notification
            notification = notificationRepository.save(notification);

            // Send SMS
            smsService.sendSms(notification.getRecipient(), notification.getContent());

            // Update status
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            return NotificationResponse.builder()
                    .id(notification.getId())
                    .status(NotificationStatus.SENT)
                    .message("SMS sent successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to send SMS notification", e);
            return NotificationResponse.builder()
                    .status(NotificationStatus.FAILED)
                    .message("Failed to send SMS: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Override
    public void processFailedNotifications() {
        List<Notification> failedNotifications =
                notificationRepository.findByStatusAndRetryCountLessThan("FAILED", 3);

        for (Notification notification : failedNotifications) {
            try {
                retryNotification(notification);
            } catch (Exception e) {
                log.error("Failed to retry notification {}", notification.getId(), e);
            }
        }
    }

    private void retryNotification(Notification notification) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledAt(LocalDateTime.now().plusMinutes(notification.getRetryCount() * 5));

        notificationRepository.save(notification);
        log.info("Scheduled notification {} for retry", notification.getId());
    }
}
