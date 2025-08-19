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
public class RefundInitiatedEvent {
    String refundId;
    String originalTransactionId;
    String bookingId;
    Long userId;
    BigDecimal refundAmount;
    String reason;
    LocalDateTime initiatedAt;
}
