package com.booking.payment_service.controller;


import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import com.booking.payment_service.dto.request.RefundRequest;
import com.booking.payment_service.dto.respone.RefundResponse;
import com.booking.payment_service.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refunds")
@Slf4j
@Tag(name = "Refunds", description = "Refund processing and status")
public class RefundController {

    private RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    

    @PostMapping
    @Operation(summary = "Process refund", description = "Process a refund request for a transaction")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(@Valid @RequestBody RefundRequest request) {
        RefundResponse refund = refundService.processRefund(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builderResponse(SuccessCode.CREATED, refund));
    }

    @GetMapping("/{refundId}")
    @Operation(summary = "Get refund status", description = "Retrieve refund status by refund ID")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundStatus(@PathVariable String refundId) {
        RefundResponse refund = refundService.getRefundStatus(refundId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, refund));
    }
}


