package com.booking.booking_service.service.Impl;

import com.booking.booking_service.service.BookingEventPublisher;
import com.booking.common_library.constants.KafkaTopics;
import com.booking.common_library.entity.booking_event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisherImpl implements BookingEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishBookingCreated(BookingCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(KafkaTopics.BOOKING_CREATED, event.getBookingId(), event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish BookingCreatedEvent for booking: {}",
                            event.getBookingId(), throwable);
                } else {
                    log.info("Successfully published BookingCreatedEvent for booking: {}",
                            event.getBookingId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing BookingCreatedEvent for booking: {}",
                    event.getBookingId(), e);
        }
    }

    @Override
    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(KafkaTopics.BOOKING_CONFIRMED, event.getBookingId(), event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish BookingConfirmedEvent for booking: {}",
                            event.getBookingId(), throwable);
                } else {
                    log.info("Successfully published BookingConfirmedEvent for booking: {}",
                            event.getBookingId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing BookingConfirmedEvent for booking: {}",
                    event.getBookingId(), e);
        }
    }

    @Override
    public void publishBookingCancelled(BookingCancelledEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(KafkaTopics.BOOKING_CANCELLED, event.getBookingId(), event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish BookingCancelledEvent for booking: {}",
                            event.getBookingId(), throwable);
                } else {
                    log.info("Successfully published BookingCancelledEvent for booking: {}",
                            event.getBookingId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing BookingCancelledEvent for booking: {}",
                    event.getBookingId(), e);
        }
    }

    @Override
    public void publishBookingExpired(BookingExpiredEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(KafkaTopics.BOOKING_EXPIRED, event.getBookingId(), event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish BookingExpiredEvent for booking: {}",
                            event.getBookingId(), throwable);
                } else {
                    log.info("Successfully published BookingExpiredEvent for booking: {}",
                            event.getBookingId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing BookingExpiredEvent for booking: {}",
                    event.getBookingId(), e);
        }
    }

    @Override
    public void publishPaymentRequested(PaymentRequestedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(KafkaTopics.PAYMENT_REQUESTED, String.valueOf(event.getBookingId()), event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish PaymentRequestedEvent for booking: {}",
                            event.getBookingId(), throwable);
                } else {
                    log.info("Successfully published PaymentRequestedEvent for booking: {}",
                            event.getBookingId());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing PaymentRequestedEvent for booking: {}",
                    event.getBookingId(), e);
        }
    }
}
