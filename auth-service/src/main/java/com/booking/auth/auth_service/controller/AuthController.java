package com.booking.auth.auth_service.controller;

import com.booking.auth.auth_service.dto.request.LoginRequest;
import com.booking.auth.auth_service.dto.request.PasswordResetRequest;
import com.booking.auth.auth_service.dto.request.RegisterRequest;
import com.booking.auth.auth_service.dto.respone.AuthResponse;
import com.booking.auth.auth_service.dto.respone.TokenResponse;
import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.booking.auth.auth_service.service.AuthService;
import com.booking.auth.auth_service.service.UserService;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;


    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builderResponse(SuccessCode.REGISTER, response));
    }



    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "Account locked")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.LOGIN, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "Refresh token") @RequestParam(required = false) String refreshToken) {

        authService.logout(refreshToken);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.LOGOUT, null));
    }


    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Parameter(description = "Refresh token") @RequestParam() String refreshToken) {

        TokenResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.TOKEN_REFRESH, response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email to user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email not found")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "Email address") @RequestParam String email) {

        authService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.VERIFY_FORGOT_PASSWORD, null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired reset token")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.CHANGE_PASSWORD, null));
    }



    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email using verification token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Verification token") @RequestParam String token) {

        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.EMAIL_VERIFIED, null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification", description = "Resend email verification to current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification email sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email already verified")
    })
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification() {
        userService.resendEmailVerification();
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.EMAIL_VERIFIED, null));
    }



    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
