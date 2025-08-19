package com.booking.payment_service.saga;

import com.booking.payment_service.utils.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPaymentSaga {
    private String sagaId;
    private String bookingId;
    private String transactionId;
    private Long userId;
    private SagaStatus status;
    private List<SagaStep> steps;
    private Map<String, Object> context;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;
    private int retryCount;
}