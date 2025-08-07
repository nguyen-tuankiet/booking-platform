package com.booking.auth.auth_service.service.Impl;

import com.booking.auth.auth_service.config.JwtConfig;
import com.booking.auth.auth_service.dto.request.LoginRequest;
import com.booking.auth.auth_service.dto.request.PasswordResetRequest;
import com.booking.auth.auth_service.dto.request.RegisterRequest;
import com.booking.auth.auth_service.dto.respone.AuthResponse;
import com.booking.auth.auth_service.dto.respone.TokenResponse;
import com.booking.auth.auth_service.dto.respone.UserResponse;
import com.booking.auth.auth_service.entity.RefreshToken;
import com.booking.auth.auth_service.entity.Role;
import com.booking.auth.auth_service.entity.User;
import com.booking.auth.auth_service.entity.VerificationToken;
import com.booking.auth.auth_service.repository.RefreshTokenRepository;
import com.booking.auth.auth_service.repository.VerificationTokenRepository;
import com.booking.auth.auth_service.security.UserPrincipal;
import com.booking.auth.auth_service.service.RedisService;
import com.booking.auth.auth_service.utils.RoleName;
import com.booking.auth.auth_service.utils.UserStatus;
import com.booking.auth.auth_service.service.EmailService;
import com.booking.auth.auth_service.repository.RoleRepository;
import com.booking.auth.auth_service.repository.UserRepository;
import com.booking.auth.auth_service.service.AuthService;
import com.booking.common_library.exception.BusinessException;
import com.booking.common_library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtUtils;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final RedisService redisService;

    @Value("${app.password-reset.expiration}")
    private long passwordResetExpirationMs;

    @Value("${app.email-verification.expiration}")
    private long emailVerificationExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        if (!request.isPasswordConfirmed()) {
            throw new BusinessException("Passwords do not match", "PASSWORD_MISMATCH");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken", "USERNAME_EXISTS");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered", "EMAIL_EXISTS");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .userStatus(UserStatus.PENDING)
                .emailVerified(false)
                .build();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BusinessException("Default role not found", "ROLE_NOT_FOUND"));
        user.setRoles(Set.of(userRole));
        user = userRepository.save(user);
        sendEmailVerification(user);
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtUtils.generateAccessToken(userPrincipal);
//        String refreshToken = createRefreshToken(user, request.getDeviceInfo());
        return AuthResponse.builder()
                .accessToken(accessToken)
//                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getTokenExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        log.info("User login attempt: {}", request.getUsernameOrEmail());
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        
        if (!user.isAccountNonLocked()) {
            throw new BusinessException("Account is temporarily locked", "ACCOUNT_LOCKED");
        }
        
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            String statusMessage = switch (user.getUserStatus()) {
                case PENDING -> "Account is pending verification. Please check your email and verify your account.";
                case SUSPENDED -> "Account is suspended. Please contact support for assistance.";
                case BANNED -> "Account is banned. Please contact support for assistance.";
                case DELETED -> "Account has been deleted.";
                default -> "Account is not active.";
            };
            throw new BusinessException(statusMessage, "ACCOUNT_NOT_ACTIVE");
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );
            if (user.getFailedLoginAttempts() > 0) {
                userRepository.updateFailedLoginAttempts(user.getId(), 0);
            }
            userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String accessToken = jwtUtils.generateAccessToken(userPrincipal);
            String refreshToken = createRefreshToken(user, request.getDeviceInfo(), ipAddress);
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtUtils.getTokenExpirationTime())
                    .user(mapToUserResponse(user))
                    .build();
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
        if (!token.isValid()) {
            refreshTokenRepository.delete(token);
            throw new BusinessException("Refresh token is expired or revoked", "EXPIRED_REFRESH_TOKEN");
        }
        User user = token.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = jwtUtils.generateAccessToken(userPrincipal);
        String newRefreshToken = createRefreshToken(user, token.getDeviceInfo(), token.getIpAddress());
        refreshTokenRepository.revokeToken(token.getToken(), LocalDateTime.now());
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtils.getTokenExpirationTime())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.revokeToken(refreshToken, LocalDateTime.now());
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
        redisService.deletePattern("user:session:" + userId + ":*");
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(passwordResetExpirationMs * 1_000_000);
        VerificationToken resetToken = VerificationToken.builder()
                .token(token)
                .type(VerificationToken.TokenType.PASSWORD_RESET)
                .user(user)
                .expiresAt(expiryDate)
                .build();
        verificationTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), token);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!request.isPasswordConfirmed()) {
            throw new BusinessException("Passwords do not match", "PASSWORD_MISMATCH");
        }
        VerificationToken token = verificationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid reset token", "INVALID_RESET_TOKEN"));
        if (!token.isValid() || token.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            throw new BusinessException("Reset token is expired or invalid", "EXPIRED_RESET_TOKEN");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        verificationTokenRepository.markTokenAsUsed(token.getToken(), LocalDateTime.now());
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        userRepository.updateFailedLoginAttempts(user.getId(), attempts);
        if (attempts >= 5) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
            userRepository.lockUserAccount(user.getId(), lockUntil);
            log.warn("Account locked for user: {} due to failed login attempts", user.getUsername());
        }
    }

    private String createRefreshToken(User user, String deviceInfo) {
        return createRefreshToken(user, deviceInfo, null);
    }

    private String createRefreshToken(User user, String deviceInfo, String ipAddress) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String tokenValue = jwtUtils.generateRefreshToken(userPrincipal);
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(jwtUtils.getRefreshTokenExpirationTime() * 1_000_000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(expiryDate)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private void sendEmailVerification(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusNanos(emailVerificationExpirationMs * 1_000_000);
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .user(user)
                .expiresAt(expiryDate)
                .build();
        verificationTokenRepository.save(verificationToken);
        emailService.sendEmailVerification(user.getEmail(), user.getFirstName(), token);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }
}
