package com.booking.auth.auth_service.controller;

import com.booking.auth.auth_service.service.EmailService;
import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@Tag(name = "Email Service", description = "Email notification endpoints")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/booking-confirmation")
    @Operation(summary = "Send booking confirmation email", description = "Send email after successful booking")
    public ResponseEntity<ApiResponse<Void>> sendBookingConfirmation(@RequestBody Map<String, String> emailData) {
        try {
            emailService.sendBookingConfirmationEmail(
                    emailData.get("toEmail"),
                    emailData.get("firstName"),
                    emailData.get("bookingReference"),
                    emailData.get("flightNumber"),
                    emailData.get("route"),
                    emailData.get("departureTime"),
                    emailData.get("totalAmount"),
                    emailData.get("seatNumbers")
            );

            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.CREATED, null));
        } catch (Exception e) {
            log.error("Error sending booking confirmation email", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<Void>builderResponse(SuccessCode.FETCHED, null));
        }
    }

    @PostMapping("/booking-cancellation")
    @Operation(summary = "Send booking cancellation email", description = "Send email after booking cancellation")
    public ResponseEntity<ApiResponse<Void>> sendBookingCancellation(@RequestBody Map<String, String> emailData) {
        try {
            emailService.sendBookingCancellationEmail(
                    emailData.get("toEmail"),
                    emailData.get("firstName"),
                    emailData.get("bookingReference"),
                    emailData.get("flightNumber"),
                    emailData.get("route"),
                    emailData.get("departureTime"),
                    emailData.get("cancellationReason")
            );

            return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.CREATED, null));
        } catch (Exception e) {
            log.error("Error sending booking cancellation email", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.<Void>builderResponse(SuccessCode.FETCHED, null));
        }
    }
}