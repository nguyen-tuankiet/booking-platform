package com.booking.notification_service.service;

public interface RateLimitService {
    boolean isAllowed(String type, String userId);
}
