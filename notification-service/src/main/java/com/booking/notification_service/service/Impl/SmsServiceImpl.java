package com.booking.notification_service.service.Impl;


import com.booking.notification_service.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Pattern;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    @Value("${sms.mock-mode:true}")
    private boolean mockMode;

    private final RestTemplate restTemplate;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    public SmsServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        try {
            if (mockMode) {
                sendMockSms(phoneNumber, message);
            } else {
                sendViaTwilio(phoneNumber, message);
            }

            log.info("SMS sent successfully to: {}", phoneNumber);

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }


    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    private void sendViaTwilio(String phoneNumber, String message) {
        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty() || twilioFromNumber.isEmpty()) {
            throw new IllegalStateException("Twilio credentials not configured. Please set twilio.account-sid, twilio.auth-token, and twilio.from-number");
        }

        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", twilioAccountSid);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(twilioAccountSid, twilioAuthToken);

            String body = String.format("From=%s&To=%s&Body=%s",
                    twilioFromNumber, phoneNumber, message);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Twilio API error: " + response.getBody());
            }

            log.debug("Twilio SMS sent successfully. Response: {}", response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via Twilio: " + e.getMessage(), e);
        }
    }

    private void sendMockSms(String phoneNumber, String message) {
        // Mock SMS service - for development/testing
        try {
            log.info("MOCK SMS - Sending to {}: {}", phoneNumber, message);

            // Simulate SMS sending delay
            Thread.sleep(100);

            // Mock success/failure based on phone number
            if (phoneNumber.endsWith("000")) {
                throw new RuntimeException("Mock: Invalid phone number");
            }

            log.info("MOCK SMS - Sent successfully to: {}", phoneNumber);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("SMS sending interrupted", e);
        }
    }
}
