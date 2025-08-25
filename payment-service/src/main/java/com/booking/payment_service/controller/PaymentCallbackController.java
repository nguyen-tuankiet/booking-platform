package com.booking.payment_service.controller;


import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import com.booking.payment_service.dto.respone.PaymentCallbackRequest;
import com.booking.payment_service.dto.respone.PaymentResponse;
import com.booking.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/callback")
@Slf4j
@Tag(name = "Payment Callback", description = "Endpoints to receive payment gateway callbacks")
public class PaymentCallbackController {

    private PaymentService paymentService;

    public PaymentCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    

    @PostMapping("/{gateway}")
    @Operation(summary = "Handle payment gateway callback", description = "Process callback from payment gateway")
    public ResponseEntity<ApiResponse<PaymentResponse>> handleCallback(
            @PathVariable String gateway,
            @RequestBody PaymentCallbackRequest callback
    ) {
        PaymentResponse response = paymentService.handleCallback(gateway, callback);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, response));
    }
}


