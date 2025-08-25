package com.booking.payment_service.controller;


import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import com.booking.payment_service.dto.respone.TransactionHistoryResponse;
import com.booking.payment_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction history and details")
public class TransactionController {

    private final TransactionService transactionService;

    

    @GetMapping
    @Operation(summary = "Get transaction history", description = "Get paginated transaction history for current user")
    public ResponseEntity<ApiResponse<Page<TransactionHistoryResponse>>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = 1L; // TODO: read from security context
        Page<TransactionHistoryResponse> history = transactionService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, history));
    }
}


