package com.booking.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFallback() {
        return createFallbackResponse("The requested resource was not found");
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> postFallback() {
        return createFallbackResponse("The requested resource was not found");
    }

    @GetMapping("/auth-service")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return createServiceUnavailableResponse("Authentication service is currently unavailable");
    }

    @GetMapping("/booking-service")
    public ResponseEntity<Map<String, Object>> bookingServiceFallback() {
        return createServiceUnavailableResponse("Booking service is currently unavailable");
    }

    @GetMapping("/payment-service")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createServiceUnavailableResponse("Payment service is currently unavailable");
    }

    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return createServiceUnavailableResponse("Notification service is currently unavailable");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> response = Map.of(
                "error", "Not Found",
                "message", message,
                "status", 404,
                "timestamp", Instant.now().toString()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    private ResponseEntity<Map<String, Object>> createServiceUnavailableResponse(String message) {
        Map<String, Object> response = Map.of(
                "error", "Service Unavailable",
                "message", message,
                "status", 503,
                "timestamp", Instant.now().toString(),
                "retry_after", "Please try again in a few moments"
        );

        log.error("Service unavailable: {}", message);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}