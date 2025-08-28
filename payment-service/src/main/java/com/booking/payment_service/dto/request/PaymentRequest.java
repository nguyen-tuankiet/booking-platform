package com.booking.payment_service.dto.request;

import com.booking.payment_service.utils.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {

    @NotNull(message = "Booking ID cannot be null")
    @Positive(message = "Booking ID must be positive")
    String bookingId;
    Long userId;
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount;

    @Builder.Default
    String currency = "VND";

    @NotNull(message = "Payment method cannot be null")
    PaymentMethod paymentMethod;

    String paymentGateway;

    String description;

    String returnUrl;

    String cancelUrl;

    Map<String, String> metadata;

    @Builder.Default
    Boolean isPriority = false;
}