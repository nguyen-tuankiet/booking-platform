package com.booking.notification_service.listener;

import com.booking.common_library.constants.KafkaConsumerGroups;
import com.booking.common_library.constants.KafkaTopics;
import com.booking.common_library.entity.booking_event.BookingCancelledEvent;
import com.booking.common_library.entity.booking_event.BookingConfirmedEvent;
import com.booking.common_library.entity.booking_event.BookingCreatedEvent;
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
@Slf4j
@RequiredArgsConstructor
public class BookingEventConsumer {
    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.BOOKING_CREATED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handleBookingCreated(
            @Payload BookingCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        try {
            log.info("Received booking created event: {}", event.getBookingId());

            // Send email confirmation
            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .userId(event.getUserId().toString())
                    .to(event.getPassengerEmail())
                    .templateName("booking-created")
                    .templateData(Map.of(
                            "bookingReference", event.getBookingReference(),
                            "flightId", event.getFlightId(),
                            "totalAmount", event.getTotalAmount(),
                            "currency", event.getCurrency(),
                            "seatNumbers", String.join(", ", event.getSeatNumbers())
                    ))
                    .priority(7)
                    .build();

            notificationService.sendEmail(emailRequest);

            // Send SMS notification
            if (event.getPassengerPhone() != null) {
                SmsNotificationRequest smsRequest = SmsNotificationRequest.builder()
                        .userId(event.getUserId().toString())
                        .phoneNumber(event.getPassengerPhone())
                        .templateName("booking-created-sms")
                        .priority(7)
                        .build();

                notificationService.sendSms(smsRequest);
            }

            ack.acknowledge();
            log.info("Successfully processed booking created event: {}", event.getBookingId());

        } catch (Exception e) {
            log.error("Failed to process booking created event: {}", event.getBookingId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CONFIRMED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE)
    public void handleBookingConfirmed(
            @Payload BookingConfirmedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        try {
            log.info("Received booking confirmed event: {}", event.getBookingId());

            // Send confirmation email with ticket details
            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .userId(event.getUserId().toString())
                    .templateName("booking-confirmed")
                    .templateData(Map.of(
                            "bookingReference", event.getBookingReference(),
                            "flightId", event.getFlightId(),
                            "seatNumbers", String.join(", ", event.getSeatNumbers()),
                            "paidAmount", event.getPaidAmount(),
                            "transactionId", event.getTransactionId(),
                            "confirmedAt", event.getConfirmedAt().toString()
                    ))
                    .priority(9)
                    .build();

            notificationService.sendEmail(emailRequest);

            ack.acknowledge();
            log.info("Successfully processed booking confirmed event: {}", event.getBookingId());

        } catch (Exception e) {
            log.error("Failed to process booking confirmed event: {}", event.getBookingId(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_CANCELLED, groupId = KafkaConsumerGroups.NOTIFICATION_SERVICE )
    public void handleBookingCancelled(
            @Payload BookingCancelledEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack) {

        try {
            log.info("Received booking cancelled event: {}", event.getBookingId());

            EmailNotificationRequest emailRequest = EmailNotificationRequest.builder()
                    .userId(event.getUserId().toString())
                    .templateName("booking-cancelled")
                    .templateData(Map.of(
                            "bookingReference", event.getBookingReference(),
                            "flightId", event.getFlightId(),
                            "cancellationReason", event.getCancellationReason(),
                            "refundRequired", event.isRefundRequired() ? "Yes" : "No",
                            "cancelledAt", event.getCancelledAt().toString()
                    ))
                    .priority(8)
                    .build();

            notificationService.sendEmail(emailRequest);

            ack.acknowledge();
            log.info("Successfully processed booking cancelled event: {}", event.getBookingId());

        } catch (Exception e) {
            log.error("Failed to process booking cancelled event: {}", event.getBookingId(), e);
        }
    }

}
