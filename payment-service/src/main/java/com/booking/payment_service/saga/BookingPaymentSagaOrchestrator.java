package com.booking.payment_service.saga;


import com.booking.common_library.entity.payment_event.PaymentFailedEvent;
import com.booking.common_library.entity.payment_event.PaymentInitiatedEvent;
import com.booking.payment_service.service.PaymentEventPublisher;
import com.booking.payment_service.utils.SagaStatus;
import com.booking.payment_service.utils.StepStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingPaymentSagaOrchestrator {

    private final PaymentEventPublisher eventPublisher;
    private final Map<String, BookingPaymentSaga> activeSagas = new HashMap<>();

    public void startBookingPaymentSaga(String bookingId, String transactionId, Long userId) {
        String sagaId = generateSagaId();

        BookingPaymentSaga saga = BookingPaymentSaga.builder()
                .sagaId(sagaId)
                .bookingId(bookingId)
                .transactionId(transactionId)
                .userId(userId)
                .status(SagaStatus.STARTED)
                .steps(createSagaSteps())
                .context(new HashMap<>())
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();

        activeSagas.put(sagaId, saga);

        log.info("Started BookingPaymentSaga: {} for booking: {}", sagaId, bookingId);
        executeNextStep(saga);
    }

    private List<SagaStep> createSagaSteps() {
        List<SagaStep> steps = new ArrayList<>();

        // Step 1: Validate booking
        steps.add(SagaStep.builder()
                .stepId("validate-booking")
                .stepName("Validate Booking")
                .status(StepStatus.PENDING)
                .serviceEndpoint("booking-service/validate")
                .compensationEndpoint("booking-service/release-seats")
                .build());

        // Step 2: Process payment
        steps.add(SagaStep.builder()
                .stepId("process-payment")
                .stepName("Process Payment")
                .status(StepStatus.PENDING)
                .serviceEndpoint("payment-gateway/process")
                .compensationEndpoint("payment-gateway/refund")
                .build());

        // Step 3: Confirm booking
        steps.add(SagaStep.builder()
                .stepId("confirm-booking")
                .stepName("Confirm Booking")
                .status(StepStatus.PENDING)
                .serviceEndpoint("booking-service/confirm")
                .compensationEndpoint("booking-service/cancel")
                .build());

        return steps;
    }

    private void executeNextStep(BookingPaymentSaga saga) {
        SagaStep nextStep = getNextPendingStep(saga);

        if (nextStep == null) {
            // All steps completed
            completeSaga(saga);
            return;
        }

        saga.setStatus(SagaStatus.IN_PROGRESS);
        nextStep.setStatus(StepStatus.IN_PROGRESS);
        nextStep.setExecutedAt(LocalDateTime.now());

        log.info("Executing step: {} for saga: {}", nextStep.getStepName(), saga.getSagaId());

        try {
            executeStep(saga, nextStep);
        } catch (Exception e) {
            log.error("Failed to execute step: {} for saga: {}", nextStep.getStepName(), saga.getSagaId(), e);
            handleStepFailure(saga, nextStep, e.getMessage());
        }
    }

    private void executeStep(BookingPaymentSaga saga, SagaStep step) {
        switch (step.getStepId()) {
            case "validate-booking":
                executeValidateBooking(saga, step);
                break;
            case "process-payment":
                executeProcessPayment(saga, step);
                break;
            case "confirm-booking":
                executeConfirmBooking(saga, step);
                break;
            default:
                throw new IllegalArgumentException("Unknown step: " + step.getStepId());
        }
    }

    private void executeValidateBooking(BookingPaymentSaga saga, SagaStep step) {
        // Simulate booking validation
        log.info("Validating booking: {} for saga: {}", saga.getBookingId(), saga.getSagaId());

        // In real implementation, call booking service to validate
        step.setStatus(StepStatus.COMPLETED);
        executeNextStep(saga);
    }

    private void executeProcessPayment(BookingPaymentSaga saga, SagaStep step) {
        log.info("Processing payment for transaction: {} in saga: {}", saga.getTransactionId(), saga.getSagaId());

        // Publish payment initiated event
        PaymentInitiatedEvent event = PaymentInitiatedEvent.builder()
                .transactionId(saga.getTransactionId())
                .bookingId(saga.getBookingId())
                .userId(saga.getUserId())
                .gateway("vnpay")
                .initiatedAt(LocalDateTime.now())
                .build();

        eventPublisher.publishPaymentInitiated(event);

        step.setStatus(StepStatus.COMPLETED);
        executeNextStep(saga);
    }

    private void executeConfirmBooking(BookingPaymentSaga saga, SagaStep step) {
        log.info("Confirming booking: {} for saga: {}", saga.getBookingId(), saga.getSagaId());

        // In real implementation, call booking service to confirm
        step.setStatus(StepStatus.COMPLETED);
        executeNextStep(saga);
    }

    private SagaStep getNextPendingStep(BookingPaymentSaga saga) {
        return saga.getSteps().stream()
                .filter(step -> step.getStatus() == StepStatus.PENDING)
                .findFirst()
                .orElse(null);
    }

    private void completeSaga(BookingPaymentSaga saga) {
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setUpdatedAt(LocalDateTime.now());

        log.info("Completed BookingPaymentSaga: {} for booking: {}", saga.getSagaId(), saga.getBookingId());

        // Remove from active sagas
        activeSagas.remove(saga.getSagaId());
    }

    private void handleStepFailure(BookingPaymentSaga saga, SagaStep step, String reason) {
        step.setStatus(StepStatus.FAILED);
        step.setFailureReason(reason);

        saga.setFailureReason(reason);
        saga.setRetryCount(saga.getRetryCount() + 1);

        log.error("Step failed: {} for saga: {}, reason: {}", step.getStepName(), saga.getSagaId(), reason);

        if (saga.getRetryCount() < 3) {
            // Retry the step
            log.info("Retrying step: {} for saga: {}, attempt: {}",
                    step.getStepName(), saga.getSagaId(), saga.getRetryCount());
            step.setStatus(StepStatus.PENDING);
            executeNextStep(saga);
        } else {
            // Start compensation
            startCompensation(saga);
        }
    }

    private void startCompensation(BookingPaymentSaga saga) {
        saga.setStatus(SagaStatus.COMPENSATING);
        log.info("Starting compensation for saga: {}", saga.getSagaId());

        // Execute compensation for completed steps in reverse order
        List<SagaStep> completedSteps = saga.getSteps().stream()
                .filter(step -> step.getStatus() == StepStatus.COMPLETED)
                .sorted((a, b) -> b.getExecutedAt().compareTo(a.getExecutedAt()))
                .toList();

        for (SagaStep step : completedSteps) {
            executeCompensation(saga, step);
        }

        saga.setStatus(SagaStatus.COMPENSATED);
        saga.setUpdatedAt(LocalDateTime.now());

        // Publish payment failed event
        PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                .transactionId(saga.getTransactionId())
                .bookingId(saga.getBookingId())
                .userId(saga.getUserId())
                .reason(saga.getFailureReason())
                .failedAt(LocalDateTime.now())
                .retryable(false)
                .build();

        eventPublisher.publishPaymentFailed(failedEvent);

        log.info("Completed compensation for saga: {}", saga.getSagaId());
        activeSagas.remove(saga.getSagaId());
    }

    private void executeCompensation(BookingPaymentSaga saga, SagaStep step) {
        log.info("Executing compensation for step: {} in saga: {}", step.getStepName(), saga.getSagaId());

        try {
            switch (step.getStepId()) {
                case "validate-booking":
                    // Release seats
                    log.info("Compensating: Releasing seats for booking: {}", saga.getBookingId());
                    break;
                case "process-payment":
                    // Refund payment
                    log.info("Compensating: Refunding payment for transaction: {}", saga.getTransactionId());
                    break;
                case "confirm-booking":
                    // Cancel booking
                    log.info("Compensating: Cancelling booking: {}", saga.getBookingId());
                    break;
            }

            step.setStatus(StepStatus.COMPENSATED);
        } catch (Exception e) {
            log.error("Failed to compensate step: {} for saga: {}", step.getStepName(), saga.getSagaId(), e);
        }
    }

    private String generateSagaId() {
        return "SAGA-" + System.currentTimeMillis() + "-" + Math.random();
    }

    // Public method để handle payment callback
    public void handlePaymentCallback(String transactionId, boolean success, String reason) {
        BookingPaymentSaga saga = activeSagas.values().stream()
                .filter(s -> transactionId.equals(s.getTransactionId()))
                .findFirst()
                .orElse(null);

        if (saga == null) {
            log.warn("No active saga found for transaction: {}", transactionId);
            return;
        }

        if (success) {
            // Continue with next step
            executeNextStep(saga);
        } else {
            // Handle failure
            SagaStep currentStep = saga.getSteps().stream()
                    .filter(step -> step.getStatus() == StepStatus.IN_PROGRESS)
                    .findFirst()
                    .orElse(null);

            if (currentStep != null) {
                handleStepFailure(saga, currentStep, reason);
            }
        }
    }
}