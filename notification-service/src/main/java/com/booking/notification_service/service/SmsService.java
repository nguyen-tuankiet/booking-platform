package com.booking.notification_service.service;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
