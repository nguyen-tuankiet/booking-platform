package com.booking.payment_service.controller;


import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import com.booking.payment_service.dto.request.OTPVerificationRequest;
import com.booking.payment_service.dto.respone.PaymentResponse;
import com.booking.payment_service.service.OTPService;
import com.booking.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/otp")
@Slf4j
@Tag(name = "OTP", description = "OTP verification and resend endpoints")
public class OTPController {

    private PaymentService paymentService;
    private OTPService otpService;

    public OTPController(PaymentService paymentService, OTPService otpService) {
        this.paymentService = paymentService;
        this.otpService = otpService;
    }

    

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP for a transaction")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyOTP(@Valid @RequestBody OTPVerificationRequest request) {
        PaymentResponse response = paymentService.verifyOTP(request);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, response));
    }

    @PostMapping("/resend/{transactionId}")
    @Operation(summary = "Resend OTP", description = "Resend OTP for a transaction")
    public ResponseEntity<ApiResponse<Void>> resendOTP(@PathVariable String transactionId) {
        otpService.generateAndSendOTP(transactionId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, null));
    }
}


