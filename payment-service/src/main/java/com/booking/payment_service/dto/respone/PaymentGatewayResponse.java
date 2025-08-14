package com.booking.payment_service.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentGatewayResponse {
    String name;
    String displayName;
    Boolean isEnabled;
    BigDecimal minAmount;
    BigDecimal maxAmount;
    String[] supportedCurrencies;
    Integer priorityOrder;
}