package com.booking.payment_service.controller;


import com.booking.common_library.dto.ApiResponse;
import com.booking.payment_service.dto.request.PaymentRequest;
import com.booking.payment_service.dto.request.OTPVerificationRequest;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.*;
import com.booking.payment_service.service.PaymentService;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
//@RequestMapping("/api/payments")
@Slf4j
@Tag(name = "Payment Management", description = "APIs for payment processing and management")
public class PaymentController {

    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    

    @GetMapping("/gateways")
    @Operation(summary = "Get available payment gateways",
            description = "Retrieve list of available payment gateways based on amount and currency")
    public ResponseEntity<ApiResponse<List<PaymentGatewayResponse>>> getAvailableGateways(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "VND") String currency) {

        List<PaymentGatewayResponse> gateways = paymentService.getAvailableGateways(amount, currency);

        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, gateways));
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create payment session",
            description = "Create a payment session for a booking")
    public ResponseEntity<ApiResponse<PaymentSessionResponse>> createPaymentSession(
            @Valid @RequestBody PaymentRequest request) {

        PaymentSessionResponse session = paymentService.createPaymentSession(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builderResponse(SuccessCode.CREATED, session));
    }

    @PostMapping("/process")
    @Operation(summary = "Process payment",
            description = "Process payment for a booking")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse payment = paymentService.processPayment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builderResponse(SuccessCode.CREATED, payment));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP",
            description = "Verify OTP for priority payment transactions")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyOTP(
            @Valid @RequestBody OTPVerificationRequest request) {

        PaymentResponse response = paymentService.verifyOTP(request);

        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, response));
    }

    @PostMapping("/refund")
    @Operation(summary = "Process refund",
            description = "Process refund for a successful transaction")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {

        RefundResponse refund = paymentService.processRefund(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builderResponse(SuccessCode.CREATED, refund));
    }

    @GetMapping("/history")
    @Operation(summary = "Get transaction history",
            description = "Get paginated transaction history for current user")
    public ResponseEntity<ApiResponse<Page<TransactionHistoryResponse>>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // In a real application, get userId from security context
        Long userId = 1L;

        Page<TransactionHistoryResponse> history = paymentService.getTransactionHistory(userId, pageable);

        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, history));
    }
}

