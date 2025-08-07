package com.booking.auth.auth_service.service.Impl;

import com.booking.auth.auth_service.dto.request.ChangePasswordRequest;
import com.booking.auth.auth_service.dto.respone.UserResponse;
import com.booking.auth.auth_service.entity.User;
import com.booking.auth.auth_service.entity.VerificationToken;
import com.booking.auth.auth_service.repository.RefreshTokenRepository;
import com.booking.auth.auth_service.repository.UserRepository;
import com.booking.auth.auth_service.repository.VerificationTokenRepository;
import com.booking.auth.auth_service.security.UserPrincipal;
import com.booking.auth.auth_service.service.EmailService;
import com.booking.auth.auth_service.service.UserService;
import com.booking.auth.auth_service.utils.UserStatus;
import com.booking.common_library.dto.PageResponse;
import com.booking.common_library.exception.BusinessException;
import com.booking.common_library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return mapToUserResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll((org.springframework.data.domain.Pageable) pageable);
        return mapToPageResponse(users);
    }

    @Override
    public PageResponse<UserResponse> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, (org.springframework.data.domain.Pageable) pageable);
        return mapToPageResponse(users);
    }

    @Override
    public PageResponse<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.findByUserStatus(status, (org.springframework.data.domain.Pageable) pageable);
        return mapToPageResponse(users);
    }


    @Override
    public void changePassword(ChangePasswordRequest request) {
        if (!request.isPasswordConfirmed()) {
            throw new BusinessException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        log.info("Password changed for user: {}", user.getUsername());
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token", "INVALID_VERIFICATION_TOKEN"));

        if (!verificationToken.isValid() || verificationToken.getType() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            throw new BusinessException("Verification token is expired or invalid", "EXPIRED_VERIFICATION_TOKEN");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        if (user.getUserStatus() == UserStatus.PENDING) {
            user.setUserStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        verificationTokenRepository.markTokenAsUsed(token, LocalDateTime.now());

        log.info("Email verified for user: {}", user.getUsername());
    }

    @Override
    public void resendEmailVerification() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (user.isEmailVerified()) {
            throw new BusinessException("Email is already verified", "EMAIL_ALREADY_VERIFIED");
        }

        verificationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(expiryDate)
                .build();

        verificationTokenRepository.save(verificationToken);
        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), token);

        log.info("Email verification resent for user: {}", user.getUsername());
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    public void deactivateAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        user.setUserStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        log.info("Account deactivated for user: {}", user.getUsername());
    }

    @Override
    public void deleteAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        user.setUserStatus(UserStatus.DELETED);
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        user.setUsername(user.getUsername() + "_deleted_" + System.currentTimeMillis());
        userRepository.save(user);

        refreshTokenRepository.deleteByUser(user);
        verificationTokenRepository.deleteByUser(user);

        log.info("Account deleted for user: {}", user.getUsername());
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }

    private PageResponse<UserResponse> mapToPageResponse(Page<User> users) {
        return PageResponse.<UserResponse>builder()
                .content(users.getContent().stream()
                        .map(this::mapToUserResponse)
                        .collect(Collectors.toList()))
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();
    }
}
