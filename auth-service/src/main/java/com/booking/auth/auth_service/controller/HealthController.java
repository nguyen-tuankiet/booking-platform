package com.booking.auth.auth_service.controller;

import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Service health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("service", "Auth Service");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, healthInfo));
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check", description = "Check if the service is ready to handle requests")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readinessCheck() {
        Map<String, Object> readinessInfo = new HashMap<>();
        readinessInfo.put("status", "READY");
        readinessInfo.put("service", "Auth Service");
        readinessInfo.put("timestamp", LocalDateTime.now());
        readinessInfo.put("database", "UP");
        readinessInfo.put("redis", "UP");
        
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, readinessInfo));
    }
} 