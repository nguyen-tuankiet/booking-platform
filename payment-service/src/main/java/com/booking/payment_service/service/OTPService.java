package com.booking.payment_service.service;

public interface OTPService {
    void generateAndSendOTP(String identifier);
    boolean verifyOTP(String identifier, String otpCode);
    void clearOTP(String identifier);
}
