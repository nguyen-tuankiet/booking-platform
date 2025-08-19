package com.booking.common_library.entity.payment_event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentFailedEvent {
    String transactionId;
    String bookingId;
    Long userId;
    String gateway;
    String reason;
    String errorCode;
    LocalDateTime failedAt;
    boolean retryable;
}
