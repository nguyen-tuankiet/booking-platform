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
public class PaymentInitiatedEvent {
     String transactionId;
     String bookingId;
     Long userId;
     String gateway;
     BigDecimal amount;
     String currency;
     String paymentUrl;
     LocalDateTime initiatedAt;
     String sessionId;
}
