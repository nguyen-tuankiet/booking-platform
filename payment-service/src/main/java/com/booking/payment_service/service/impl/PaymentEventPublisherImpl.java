package com.booking.payment_service.service.impl;

import com.booking.common_library.constants.KafkaTopics;
import com.booking.common_library.entity.payment_event.*;
import com.booking.payment_service.service.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishPaymentInitiated(PaymentInitiatedEvent event) {
        sendEvent(KafkaTopics.PAYMENT_INITIATED, event.getTransactionId(), event,
                "PaymentInitiatedEvent for transaction");
    }

    @Override
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        sendEvent(KafkaTopics.PAYMENT_COMPLETED, event.getTransactionId(), event,
                "PaymentCompletedEvent for transaction");
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        sendEvent(KafkaTopics.PAYMENT_FAILED, event.getTransactionId(), event,
                "PaymentFailedEvent for transaction");
    }

    @Override
    public void publishOTPRequired(OTPRequiredEvent event) {
        try {
            String partitionKey = String.valueOf(event.getPriority());
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(KafkaTopics.OTP_REQUIRED, partitionKey, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish OTPRequiredEvent for transaction: {}",
                            event.getTransactionId(), throwable);
                } else {
                    log.info("Successfully published OTPRequiredEvent for transaction: {} with priority: {}",
                            event.getTransactionId(), event.getPriority());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing OTPRequiredEvent for transaction: {}", event.getTransactionId(), e);
        }
    }

    @Override
    public void publishRefundInitiated(RefundInitiatedEvent event) {
        sendEvent(KafkaTopics.REFUND_INITIATED, event.getRefundId(), event,
                "RefundInitiatedEvent for refund");
    }

    @Override
    public void publishRefundCompleted(RefundCompletedEvent event) {
        sendEvent(KafkaTopics.REFUND_COMPLETED, event.getRefundId(), event,
                "RefundCompletedEvent for refund");
    }

    private void sendEvent(String topic, String key, Object event, String logPrefix) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to publish {}: {}", logPrefix, key, throwable);
                } else {
                    log.info("Successfully published {}: {}", logPrefix, key);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing {}: {}", logPrefix, key, e);
        }
    }
}