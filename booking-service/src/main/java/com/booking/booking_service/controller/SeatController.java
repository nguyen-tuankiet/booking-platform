package com.booking.booking_service.controller;

import com.booking.booking_service.dto.request.SeatSelectionRequest;
import com.booking.booking_service.entity.SeatLock;
import com.booking.booking_service.service.BookingService;
import com.booking.booking_service.service.SeatLockService;
import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Management", description = "Seat selection and locking endpoints")
public class SeatController {

    private final BookingService bookingService;
    private final SeatLockService seatLockService;

    @PostMapping("/select")
    @Operation(summary = "Select seats", description = "Select and lock seats for booking")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seats selected and locked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid seat selection"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Seats already locked")
    })
    public ResponseEntity<ApiResponse<List<SeatLock>>> selectSeats(
            @Valid @RequestBody SeatSelectionRequest request) {

        List<SeatLock> seatLocks = bookingService.selectSeats(request);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, seatLocks));
    }

    @PostMapping("/release")
    @Operation(summary = "Release seat locks", description = "Release all seat locks for current user on specified flight")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seat locks released successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Void>> releaseSeatLocks(
            @Parameter(description = "Flight ID") @RequestParam String flightId) {

        // Get current user từ security context sẽ được implement trong service
        seatLockService.releaseUserLocks(flightId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, null));
    }

    @PostMapping("/release/session")
    @Operation(summary = "Release session locks", description = "Release all seat locks for a session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Session locks released successfully")
    })
    public ResponseEntity<ApiResponse<Void>> releaseSessionLocks(
            @Parameter(description = "Session ID") @RequestParam String sessionId) {

        seatLockService.releaseSessionLocks(sessionId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, null));
    }

    @GetMapping("/locks")
    @Operation(summary = "Get user seat locks", description = "Get all active seat locks for current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seat locks retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<SeatLock>>> getUserSeatLocks() {
        List<SeatLock> seatLocks = seatLockService.getUserActiveLocks(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, seatLocks));
    }

    @PostMapping("/{flightId}/{seatNumber}/extend")
    @Operation(summary = "Extend seat lock", description = "Extend seat lock duration")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seat lock extended successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Seat lock not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Cannot extend lock owned by another user")
    })
    public ResponseEntity<ApiResponse<Void>> extendSeatLock(
            @Parameter(description = "Flight ID") @PathVariable String flightId,
            @Parameter(description = "Seat number") @PathVariable String seatNumber,
            @Parameter(description = "Additional minutes") @RequestParam(defaultValue = "5") int additionalMinutes) {

        seatLockService.extendSeatLock(flightId, seatNumber, getCurrentUserId(), additionalMinutes);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, null));
    }

    private Long getCurrentUserId() {
        // This will be implemented properly with SecurityContext
        // For now, return a dummy value
        return 1L;
    }
}