package com.booking.common_library.constants;

public class KafkaTopics {
    // Booking Events
    public static final String BOOKING_CREATED = "booking-created";
    public static final String BOOKING_CONFIRMED = "booking-confirmed";
    public static final String BOOKING_CANCELLED = "booking-cancelled";
    public static final String BOOKING_EXPIRED = "booking-expired";
    public static final String PAYMENT_REQUESTED = "payment-requested";

    // Payment Events
    public static final String PAYMENT_INITIATED = "payment-initiated";
    public static final String PAYMENT_COMPLETED = "payment-completed";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String OTP_REQUIRED = "otp-required";
    public static final String REFUND_INITIATED = "refund-initiated";
    public static final String REFUND_COMPLETED = "refund-completed";

    // Notification Events
    public static final String SEND_EMAIL = "send-email";
    public static final String SEND_SMS = "send-sms";
    public static final String SEND_PUSH_NOTIFICATION = "send-push-notification";
}
