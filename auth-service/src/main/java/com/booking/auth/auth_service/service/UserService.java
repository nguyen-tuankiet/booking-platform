package com.booking.auth.auth_service.service;

import com.booking.auth.auth_service.dto.request.ChangePasswordRequest;
import com.booking.auth.auth_service.dto.respone.UserResponse;
import com.booking.auth.auth_service.utils.UserStatus;
import com.booking.common_library.dto.PageResponse;
import jakarta.transaction.Transactional;

import java.awt.print.Pageable;

public interface UserService {
    UserResponse getCurrentUser();

    UserResponse getUserById(Long userId);

    UserResponse getUserByUsername(String username);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    PageResponse<UserResponse> searchUsers(String keyword, Pageable pageable);

    PageResponse<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable);

    @Transactional
    void changePassword(ChangePasswordRequest request);

    @Transactional
    void verifyEmail(String token);

    @Transactional
    void resendEmailVerification();

    boolean isUsernameAvailable(String username);

    boolean isEmailAvailable(String email);

    @Transactional
    void deactivateAccount();

    @Transactional
    void deleteAccount();
}
