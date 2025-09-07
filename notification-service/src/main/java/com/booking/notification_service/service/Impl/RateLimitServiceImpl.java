package com.booking.notification_service.service.Impl;

import com.booking.notification_service.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${notification.rate-limit.email.max-per-minute:10}")
    private int emailMaxPerMinute;

    @Value("${notification.rate-limit.sms.max-per-minute:5}")
    private int smsMaxPerMinute;

    @Override
    public boolean isAllowed(String type, String userId) {
        String key = "rate_limit:notification:" + type + ":" + userId;
        int maxRequests = getMaxRequests(type);

        String countStr = (String) redisTemplate.opsForValue().get(key);
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;

        if (currentCount >= maxRequests) {
            log.warn("Rate limit exceeded for user {} type {}: {}/{}", userId, type, currentCount, maxRequests);
            return false;
        }

        // Increment counter
        redisTemplate.opsForValue().increment(key);

        // Set expiration on first request
        if (currentCount == 0) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        return true;
    }

    private int getMaxRequests(String type) {
        switch (type.toLowerCase()) {
            case "email": return emailMaxPerMinute;
            case "sms": return smsMaxPerMinute;
            default: return 20;
        }
    }
}
