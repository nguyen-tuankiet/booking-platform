package com.booking.payment_service.service.impl;

import com.booking.payment_service.service.OTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPServiceImpl implements OTPService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration:300000}")
    private long otpExpiration;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Override
    public void generateAndSendOTP(String identifier) {
        String otpCode = generateOTP();
        String key = "otp:" + identifier;
        String attemptsKey = "otp_attempts:" + identifier;

        // Store OTP in Redis with expiration
        redisTemplate.opsForValue().set(key, otpCode, otpExpiration, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(attemptsKey, "0", otpExpiration, TimeUnit.MILLISECONDS);

        // In real application, send OTP via SMS/Email
        log.info("OTP generated for {}: {} (This should be sent via SMS/Email)", identifier, otpCode);
    }

    @Override
    public boolean verifyOTP(String identifier, String otpCode) {
        String key = "otp:" + identifier;
        String attemptsKey = "otp_attempts:" + identifier;

        // Check attempts
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : maxAttempts;

        if (attempts >= maxAttempts) {
            log.warn("Maximum OTP attempts exceeded for identifier: {}", identifier);
            return false;
        }

        // Verify OTP
        String storedOTP = redisTemplate.opsForValue().get(key);
        if (storedOTP != null && storedOTP.equals(otpCode)) {
            // Clear OTP after successful verification
            clearOTP(identifier);
            log.info("OTP verified successfully for identifier: {}", identifier);
            return true;
        } else {
            // Increment attempts
            redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1),
                    otpExpiration, TimeUnit.MILLISECONDS);
            log.warn("Invalid OTP for identifier: {}. Attempts: {}/{}", identifier, attempts + 1, maxAttempts);
            return false;
        }
    }

    @Override
    public void clearOTP(String identifier) {
        String key = "otp:" + identifier;
        String attemptsKey = "otp_attempts:" + identifier;

        redisTemplate.delete(key);
        redisTemplate.delete(attemptsKey);

        log.debug("OTP cleared for identifier: {}", identifier);
    }

    private String generateOTP() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}