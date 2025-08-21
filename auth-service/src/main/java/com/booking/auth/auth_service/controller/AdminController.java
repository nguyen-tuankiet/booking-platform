package com.booking.auth.auth_service.controller;

import com.booking.auth.auth_service.dto.request.UpdateUserStatusRequest;
import com.booking.auth.auth_service.dto.respone.UserResponse;
import com.booking.auth.auth_service.service.UserService;
import com.booking.auth.auth_service.utils.UserStatus;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Management", description = "Admin user management endpoints")
public class AdminController {
    
    private final UserService userService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination and filtering")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir,
            @Parameter(description = "User status filter") @RequestParam(required = false) UserStatus status) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<UserResponse> users;
        if (status != null) {
            users = userService.getUsersByStatus(status, (java.awt.print.Pageable) pageable);
        } else {
            users = userService.getAllUsers((java.awt.print.Pageable) pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, users));
    }

    @GetMapping("/users/search")
    @Operation(summary = "Search users", description = "Search users by keyword with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users found successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserResponse> users = userService.searchUsers(keyword, (java.awt.print.Pageable) pageable);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve specific user details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, user));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user status (activate, deactivate, suspend)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        
        // Ensure the userId in path matches the one in request
        if (!userId.equals(request.getUserId())) {
            throw new IllegalArgumentException("User ID in path must match User ID in request body");
        }
        
        userService.updateUserStatus(request);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, null));
    }


} 