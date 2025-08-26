package com.booking.booking_service.controller;

import com.booking.booking_service.dto.request.BookingRequest;

import com.booking.booking_service.dto.respone.BookingResponse;
import com.booking.booking_service.service.BookingService;
import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.dto.PageResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
//@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Flight booking and reservation endpoints")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/test-auth")
    @Operation(summary = "Test booking authentication", description = "Test authentication within BookingController")
    public ResponseEntity<ApiResponse<String>> testBookingAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String message = "Booking Controller Auth Test - User: " + auth.getName() + ", Authorities: " + auth.getAuthorities();
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, message));
    }

    @PostMapping("/create-booking")
    @Operation(summary = "Create booking", description = "Create a new flight booking")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid booking data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Seats not available")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {

        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builderResponse(SuccessCode.CREATED, booking));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get user bookings", description = "Retrieve current user's bookings with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getUserBookings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<BookingResponse> bookings = bookingService.getUserBookings(pageable);

        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, bookings));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get booking by ID", description = "Retrieve specific booking details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable String bookingId) {

        BookingResponse booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, booking));
    }

    @GetMapping("/booking-reference/{bookingReference}")
    @Operation(summary = "Get booking by reference", description = "Retrieve booking details using booking reference")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(
            @Parameter(description = "Booking reference number") @PathVariable String bookingReference) {

        BookingResponse booking = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, booking));
    }

    @PostMapping("/cancel-booking/{bookingId}")
    @Operation(summary = "Cancel booking", description = "Cancel an existing booking")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot cancel booking in current status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable String bookingId,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {

        bookingService.cancelBooking(bookingId, reason);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, null));
    }

    @PutMapping("/confirm-booking/{bookingId}")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('ADMIN')")
    @Operation(summary = "Confirm booking", description = "Confirm booking after successful payment (System/Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Booking not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot confirm booking in current status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> confirmBooking(
            @Parameter(description = "Booking ID") @PathVariable String bookingId) {

        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, null));
    }
}