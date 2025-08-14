package com.booking.payment_service.dto.respone;

import com.booking.payment_service.utils.PaymentMethod;
import com.booking.payment_service.utils.TransactionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionHistoryResponse {
    String transactionId;
    Long bookingId;
    BigDecimal amount;
    String currency;
    TransactionStatus status;
    PaymentMethod paymentMethod;
    String paymentGateway;
    String description;
    LocalDateTime createdAt;
    LocalDateTime processedAt;
    String failedReason;
}
