package com.booking.auth.auth_service.service.Impl;

import com.booking.auth.auth_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendEmailVerification(String toEmail, String firstName, String token) {
        try {
            String subject = "Verify Your Email - " + appName;
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            String body = String.format(
                    "Hi %s,\n\n" +
                            "Thank you for registering with %s!\n\n" +
                            "Please click the link below to verify your email address:\n" +
                            "%s\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you didn't create an account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    firstName, appName, verificationUrl, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Email verification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", toEmail, e);
        }
    }

    @Override    @Async
    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        try {
            String subject = "Reset Your Password - " + appName;
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String body = String.format(
                    "Hi %s,\n\n" +
                            "We received a request to reset your password for your %s account.\n\n" +
                            "Please click the link below to reset your password:\n" +
                            "%s\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request a password reset, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    firstName, appName, resetUrl, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            String subject = "Welcome to " + appName + "!";

            String body = String.format(
                    "Hi %s,\n\n" +
                            "Welcome to %s! Your account has been successfully verified.\n\n" +
                            "You can now enjoy all the features our platform has to offer.\n\n" +
                            "If you have any questions, feel free to contact our support team.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    firstName, appName, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendAccountLockNotification(String toEmail, String firstName) {
        try {
            String subject = "Account Security Alert - " + appName;

            String body = String.format(
                    "Hi %s,\n\n" +
                            "Your %s account has been temporarily locked due to multiple failed login attempts.\n\n" +
                            "Your account will be automatically unlocked in 30 minutes.\n\n" +
                            "If this wasn't you, please consider changing your password after your account is unlocked.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    firstName, appName, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Account lock notification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send account lock notification to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendPasswordChangeNotification(String toEmail, String firstName) {
        try {
            String subject = "Password Changed - " + appName;

            String body = String.format(
                    "Hi %s,\n\n" +
                            "Your password for %s has been successfully changed.\n\n" +
                            "If you didn't make this change, please contact our support team immediately.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    firstName, appName, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Password change notification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password change notification to: {}", toEmail, e);
        }
    }

    @Override
    @Async
    public void sendBookingConfirmationEmail(String toEmail, String firstName, String bookingReference,
                                             String flightNumber, String route, String departureTime,
                                             String totalAmount, String seatNumbers) {
        try {
            String subject = "Booking Confirmation - " + bookingReference + " - " + appName;
            String bookingUrl = frontendUrl + "/bookings/" + bookingReference;

            String body = String.format(
                    "Dear %s,\n\n" +
                            "Great news! Your flight booking has been confirmed.\n\n" +
                            "BOOKING DETAILS:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Booking Reference: %s\n" +
                            "Flight: %s\n" +
                            "Route: %s\n" +
                            "Departure: %s\n" +
                            "Seat(s): %s\n" +
                            "Total Amount: $%s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "NEXT STEPS:\n" +
                            "• Check-in online 24 hours before departure\n" +
                            "• Arrive at the airport at least 2 hours before domestic flights (3 hours for international)\n" +
                            "• Bring a valid ID/passport and your booking reference\n\n" +
                            "View your complete booking details: %s\n\n" +
                            "Have a wonderful trip!\n\n" +
                            "Best regards,\n" +
                            "The %s Team\n\n" +
                            "Need help? Contact our support team 24/7.",
                    firstName, bookingReference, flightNumber, route, departureTime,
                    seatNumbers, totalAmount, bookingUrl, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Booking confirmation email sent to: {} for booking: {}", toEmail, bookingReference);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {} for booking: {}", toEmail, bookingReference, e);
        }
    }

    @Override
    @Async
    public void sendBookingCancellationEmail(String toEmail, String firstName, String bookingReference,
                                             String flightNumber, String route, String departureTime,
                                             String cancellationReason) {
        try {
            String subject = "Booking Cancelled - " + bookingReference + " - " + appName;

            String body = String.format(
                    "Dear %s,\n\n" +
                            "We're sorry to confirm that your flight booking has been cancelled.\n\n" +
                            "CANCELLED BOOKING DETAILS:\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "Booking Reference: %s\n" +
                            "Flight: %s\n" +
                            "Route: %s\n" +
                            "Original Departure: %s\n" +
                            "Cancellation Reason: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                            "REFUND INFORMATION:\n" +
                            "If you paid for this booking, your refund will be processed according to our refund policy.\n" +
                            "Refunds typically take 5-10 business days to appear on your statement.\n\n" +
                            "NEED HELP?\n" +
                            "• Book a new flight: %s/search\n" +
                            "• Contact our support team for assistance\n" +
                            "• Check our refund policy: %s/refund-policy\n\n" +
                            "We apologize for any inconvenience caused and appreciate your understanding.\n\n" +
                            "Best regards,\n" +
                            "The %s Team",
                    firstName, bookingReference, flightNumber, route, departureTime,
                    cancellationReason != null ? cancellationReason : "Requested by passenger",
                    frontendUrl, frontendUrl, appName
            );

            sendSimpleEmail(toEmail, subject, body);
            log.info("Booking cancellation email sent to: {} for booking: {}", toEmail, bookingReference);
        } catch (Exception e) {
            log.error("Failed to send booking cancellation email to: {} for booking: {}", toEmail, bookingReference, e);
        }
    }

    private void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
