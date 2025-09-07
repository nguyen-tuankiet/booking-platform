package com.booking.notification_service.service;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String content);
    void sendHtmlEmail(String to, String subject, String htmlContent);
}
