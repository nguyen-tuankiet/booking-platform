package com.booking.booking_service.listener;

import com.booking.booking_service.service.BookingService;
import com.booking.booking_service.utils.PaymentStatus;
import com.booking.common_library.constants.KafkaConsumerGroups;
import com.booking.common_library.constants.KafkaTopics;

import com.booking.common_library.entity.payment_event.PaymentCompletedEvent;
import com.booking.common_library.entity.payment_event.PaymentFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {
    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = KafkaConsumerGroups.BOOKING_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentCompleted(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received payment completed event from topic: {}, partition: {}, offset: {}",
                    topic, partition, offset);

            PaymentCompletedEvent event = objectMapper.readValue(payload, PaymentCompletedEvent.class);

            log.info("Processing payment completed for booking: {}, transaction: {}",
                    event.getBookingId(), event.getTransactionId());

            // Update booking payment status và confirm booking
            bookingService.updateBookingPaymentStatus(event.getBookingId(), PaymentStatus.COMPLETED);
            bookingService.confirmBooking(event.getBookingId());

            log.info("Successfully processed payment completed for booking: {}", event.getBookingId());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing payment completed event: {}", payload, e);
            // TODO: Implement retry logic or send to DLQ
            acknowledgment.acknowledge(); // Acknowledge to prevent reprocessing for now
        }
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = KafkaConsumerGroups.BOOKING_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received payment failed event from topic: {}, partition: {}, offset: {}",
                    topic, partition, offset);

            PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);

            log.info("Processing payment failed for booking: {}, transaction: {}, reason: {}",
                    event.getBookingId(), event.getTransactionId(), event.getReason());

            // Update booking payment status
            bookingService.updateBookingPaymentStatus(event.getBookingId(), PaymentStatus.FAILED);

            // If not retryable, expire the booking
            if (!event.isRetryable()) {
                bookingService.expireBooking(event.getBookingId());
                log.info("Booking {} expired due to non-retryable payment failure", event.getBookingId());
            }

            log.info("Successfully processed payment failed for booking: {}", event.getBookingId());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing payment failed event: {}", payload, e);
            acknowledgment.acknowledge();
        }
    }
}
