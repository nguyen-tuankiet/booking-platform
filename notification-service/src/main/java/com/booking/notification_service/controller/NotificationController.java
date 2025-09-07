package com.booking.notification_service.controller;

import com.booking.notification_service.dto.request.EmailNotificationRequest;
import com.booking.notification_service.dto.request.SmsNotificationRequest;
import com.booking.notification_service.dto.respone.NotificationResponse;
import com.booking.notification_service.entity.Notification;
import com.booking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<NotificationResponse> sendEmail(@RequestBody EmailNotificationRequest request) {
        log.info("Received email notification request for user: {}", request.getUserId());
        NotificationResponse response = notificationService.sendEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sms")
    public ResponseEntity<NotificationResponse> sendSms(@RequestBody SmsNotificationRequest request) {
        log.info("Received SMS notification request for user: {}", request.getUserId());
        NotificationResponse response = notificationService.sendSms(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        log.info("Retrying failed notifications");
        notificationService.processFailedNotifications();
        return ResponseEntity.ok("Failed notifications retry initiated");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
