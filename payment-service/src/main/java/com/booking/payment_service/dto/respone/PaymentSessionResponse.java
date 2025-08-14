package com.booking.payment_service.dto.respone;

import com.booking.payment_service.utils.PaymentSessionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSessionResponse {
    String sessionId;
    Long bookingId;
    BigDecimal amount;
    String currency;
    PaymentSessionStatus status;
    String paymentGateway;
    String paymentUrl;
    LocalDateTime expiresAt;
    LocalDateTime createdAt;
}
