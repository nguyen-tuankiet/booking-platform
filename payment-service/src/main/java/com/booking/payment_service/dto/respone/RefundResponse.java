package com.booking.payment_service.dto.respone;

import com.booking.payment_service.utils.RefundStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundResponse {
    String refundId;
    String transactionId;
    BigDecimal amount;
    RefundStatus status;
    String reason;
    LocalDateTime createdAt;
    LocalDateTime processedAt;
}
