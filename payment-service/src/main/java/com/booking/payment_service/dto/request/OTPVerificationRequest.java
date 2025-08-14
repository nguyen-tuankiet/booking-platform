package com.booking.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OTPVerificationRequest {

    @NotBlank(message = "Transaction ID cannot be blank")
    String transactionId;

    @NotBlank(message = "OTP code cannot be blank")
    @Size(min = 4, max = 8, message = "OTP code must be between 4 and 8 characters")
    String otpCode;
}
