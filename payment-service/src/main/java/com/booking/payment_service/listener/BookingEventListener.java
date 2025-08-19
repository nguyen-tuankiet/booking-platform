package com.booking.payment_service.listener;


import com.booking.common_library.constants.KafkaConsumerGroups;
import com.booking.common_library.constants.KafkaTopics;
import com.booking.common_library.entity.booking_event.BookingCancelledEvent;
import com.booking.common_library.entity.booking_event.PaymentRequestedEvent;
import com.booking.payment_service.service.PaymentService;
import com.booking.payment_service.service.RefundService;
import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.utils.PaymentMethod;
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
public class BookingEventListener {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_REQUESTED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentRequested(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received payment requested event from topic: {}, partition: {}, offset: {}",
                    topic, partition, offset);

            PaymentRequestedEvent event = objectMapper.readValue(payload, PaymentRequestedEvent.class);

            log.info("Processing payment request for booking: {}, amount: {} {}",
                    event.getBookingId(), event.getAmount(), event.getCurrency());

            // Create payment request
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(Long.valueOf(event.getBookingId()))
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .paymentMethod(PaymentMethod.valueOf(event.getPaymentMethod()))
                    .returnUrl(event.getReturnUrl())
                    .cancelUrl(event.getCancelUrl())
                    .build();

            // Process payment
            paymentService.processPayment(paymentRequest);

            log.info("Successfully initiated payment for booking: {}", event.getBookingId());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing payment requested event: {}", payload, e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(
            topics = KafkaTopics.BOOKING_CANCELLED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleBookingCancelled(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received booking cancelled event from topic: {}, partition: {}, offset: {}",
                    topic, partition, offset);

            BookingCancelledEvent event = objectMapper.readValue(payload, BookingCancelledEvent.class);

            log.info("Processing booking cancellation for booking: {}, refund required: {}",
                    event.getBookingId(), event.isRefundRequired());

            // Process refund if required and transaction exists
            if (event.isRefundRequired() && event.getTransactionId() != null) {
                RefundRequest refundRequest = RefundRequest.builder()
                        .transactionId(event.getTransactionId())
                        .reason(event.getCancellationReason())
                        .build();

                refundService.processRefund(refundRequest);
                log.info("Refund initiated for cancelled booking: {}", event.getBookingId());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing booking cancelled event: {}", payload, e);
            acknowledgment.acknowledge();
        }
    }
}
