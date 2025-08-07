package com.booking.auth.auth_service.service;

public interface EmailService {
    void sendEmailVerification(String toEmail, String firstName, String token);
    void sendPasswordResetEmail(String toEmail, String firstName, String token);
    void sendWelcomeEmail(String toEmail, String firstName);
    void sendAccountLockNotification(String toEmail, String firstName);
    void sendPasswordChangeNotification(String toEmail, String firstName);
}
