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
public class OTPRequiredEvent {
    String transactionId;
    String bookingId;
    Long userId;
    String phoneNumber;
    String email;
    LocalDateTime requestedAt;
    int priority; // 1-10, higher = more priority
}
