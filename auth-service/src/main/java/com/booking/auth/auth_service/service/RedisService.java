package com.booking.auth.auth_service.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    void setValue(String key, Object value);
    void setValue(String key, Object value, long timeout, TimeUnit unit);
    Object getValue(String key);
    boolean hasKey(String key);
    void deleteKey(String key);
    void deletePattern(String pattern);
    void expire(String key, long timeout, TimeUnit unit);
    Long getExpire(String key);

    void storeUserSession(Long userId, String sessionId, Object sessionData);
    Object getUserSession(Long userId, String sessionId);
    void deleteUserSession(Long userId, String sessionId);
    void deleteAllUserSessions(Long userId);

    boolean isRateLimited(String key, int maxRequests, long windowSeconds);

    void blacklistToken(String token, long expirationSeconds);
    boolean isTokenBlacklisted(String token);
}
