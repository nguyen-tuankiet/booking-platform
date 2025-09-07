package com.booking.notification_service.config;

import com.booking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void processFailedNotifications() {
        log.debug("Processing failed notifications");
        try {
            notificationService.processFailedNotifications();
        } catch (Exception e) {
            log.error("Error processing failed notifications", e);
        }
    }
}
