package com.booking.common_library.entity.booking_event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestedEvent {
    private String bookingId;
    private String bookingReference;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private LocalDateTime requestedAt;
    private String returnUrl;
    private String cancelUrl;
}
