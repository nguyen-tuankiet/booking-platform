package com.booking.payment_service.controller;


import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import com.booking.payment_service.dto.respone.PaymentSessionResponse;
import com.booking.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment-sessions")
@Slf4j
@Tag(name = "Payment Sessions", description = "Payment session management endpoints")
public class PaymentSessionController {

    private PaymentService paymentService;

    public PaymentSessionController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get payment session", description = "Retrieve payment session by ID")
    public ResponseEntity<ApiResponse<PaymentSessionResponse>> getSession(@PathVariable String sessionId) {
        PaymentSessionResponse response = paymentService.getPaymentSession(sessionId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, response));
    }
}


