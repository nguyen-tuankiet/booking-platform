package com.booking.auth.auth_service.service.Impl;


import com.booking.auth.auth_service.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Set value for key: {}", key);
        } catch (Exception e) {
            log.error("Error setting value for key: {}", key, e);
        }
    }

    @Override
    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Set value for key: {} with timeout: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting value with timeout for key: {}", key, e);
        }
    }

    @Override
    public Object getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting value for key: {}", key, e);
            return null;
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking if key exists: {}", key, e);
            return false;
        }
    }

    @Override
    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
        }
    }

    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Error deleting keys with pattern: {}", pattern, e);
        }
    }

    @Override
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
            log.debug("Set expiration for key: {} to {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key);
        } catch (Exception e) {
            log.error("Error getting expiration for key: {}", key, e);
            return -1L;
        }
    }

    @Override
    public void storeUserSession(Long userId, String sessionId, Object sessionData) {
        String key = "user:session:" + userId + ":" + sessionId;
        setValue(key, sessionData, 24, TimeUnit.HOURS);
    }

    @Override
    public Object getUserSession(Long userId, String sessionId) {
        String key = "user:session:" + userId + ":" + sessionId;
        return getValue(key);
    }

    @Override
    public void deleteUserSession(Long userId, String sessionId) {
        String key = "user:session:" + userId + ":" + sessionId;
        deleteKey(key);
    }

    @Override
    public void deleteAllUserSessions(Long userId) {
        String pattern = "user:session:" + userId + ":*";
        deletePattern(pattern);
    }

    @Override
    public boolean isRateLimited(String key, int maxRequests, long windowSeconds) {
        try {
            String rateLimitKey = "rate_limit:" + key;
            String currentCount = (String) getValue(rateLimitKey);

            if (currentCount == null) {
                setValue(rateLimitKey, "1", windowSeconds, TimeUnit.SECONDS);
                return false;
            }

            int count = Integer.parseInt(currentCount);
            if (count >= maxRequests) {
                return true;
            }

            redisTemplate.opsForValue().increment(rateLimitKey);
            return false;
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return false;
        }
    }

    @Override
    public void blacklistToken(String token, long expirationSeconds) {
        String key = "blacklist:token:" + token;
        setValue(key, "blacklisted", expirationSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return hasKey(key);
    }
}
