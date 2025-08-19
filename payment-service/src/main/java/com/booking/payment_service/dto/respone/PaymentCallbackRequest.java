package com.booking.payment_service.dto.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCallbackRequest {
    String gatewayTransactionId;
    String transactionId;
    String status;
    String amount;
    String currency;
    String signature;
    Map<String, String> additionalData;
    
}
