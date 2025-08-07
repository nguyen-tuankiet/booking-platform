package com.booking.auth.auth_service.service;

import com.booking.auth.auth_service.dto.request.LoginRequest;
import com.booking.auth.auth_service.dto.request.PasswordResetRequest;
import com.booking.auth.auth_service.dto.request.RegisterRequest;
import com.booking.auth.auth_service.dto.respone.AuthResponse;
import com.booking.auth.auth_service.dto.respone.TokenResponse;
import com.booking.auth.auth_service.dto.respone.UserResponse;
import com.booking.auth.auth_service.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress);

    TokenResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void logoutAll(Long userId);

    void sendPasswordResetEmail(String email);

    void resetPassword(PasswordResetRequest request);

}
