package com.booking.notification_service.listener;

import com.booking.common_library.constants.KafkaConsumerGroups;
import com.booking.common_library.constants.KafkaTopics;
import com.booking.common_library.entity.payment_event.PaymentCompletedEvent;
import com.booking.common_library.entity.payment_event.PaymentFailedEvent;
import com.booking.common_library.entity.payment_event.OTPRequiredEvent;
import com.booking.notification_service.dto.request.EmailNotificationRequest;
import com.booking.notification_service.dto.request.SmsNotificationRequest;
import com.booking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handlePaymentCompleted(
            @Payload PaymentCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        try {
            log.info("Received payment completed event: {}", event.getTransactionId());

            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .userId(event.getUserId().toString())
                    .templateName("payment-completed")
                    .templateData(Map.of(
                            "transactionId", event.getTransactionId(),
                            "bookingId", event.getBookingId(),
                            "amount", event.getAmount(),
                            "currency", event.getCurrency(),
                            "paymentMethod", event.getPaymentMethod(),
                            "completedAt", event.getCompletedAt().toString()
                    ))
                    .priority(8)
                    .build();

            notificationService.sendEmail(emailRequest);

            ack.acknowledge();
            log.info("Successfully processed payment completed event: {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process payment completed event: {}", event.getTransactionId(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handlePaymentFailed(
            @Payload PaymentFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        try {
            log.info("Received payment failed event: {}", event.getTransactionId());

            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .userId(event.getUserId().toString())
                    .templateName("payment-failed")
                    .templateData(Map.of(
                            "transactionId", event.getTransactionId(),
                            "bookingId", event.getBookingId(),
                            "reason", event.getReason(),
                            "errorCode", event.getErrorCode(),
                            "retryable", event.isRetryable() ? "Yes" : "No"
                    ))
                    .priority(9)
                    .build();

            notificationService.sendEmail(emailRequest);

            ack.acknowledge();
            log.info("Successfully processed payment failed event: {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process payment failed event: {}", event.getTransactionId(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.OTP_REQUIRED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handleOTPRequired(
            @Payload OTPRequiredEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        try {
            log.info("Received OTP required event: {}", event.getTransactionId());

            // Send high-priority SMS for OTP
            SmsNotificationRequest smsRequest = SmsNotificationRequest.builder()
                    .userId(event.getUserId().toString())
                    .phoneNumber(event.getPhoneNumber())
                    .templateName("otp-required")
                    .priority(10) // Highest priority
                    .build();

            notificationService.sendSms(smsRequest);

            // Also send email notification
            if (event.getEmail() != null) {
                EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                        .userId(event.getUserId().toString())
                        .to(event.getEmail())
                        .templateName("otp-required-email")
                        .templateData(Map.of(
                                "transactionId", event.getTransactionId(),
                                "bookingId", event.getBookingId()
                        ))
                        .priority(10)
                        .build();

                notificationService.sendEmail(emailRequest);
            }

            ack.acknowledge();
            log.info("Successfully processed OTP required event: {}", event.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to process OTP required event: {}", event.getTransactionId(), e);
        }
    }
}
