package com.booking.booking_service.service;

import com.booking.booking_service.entity.Booking;
import com.booking.booking_service.entity.Flight;

public interface EmailNotificationService {
    /**
     * Send booking confirmation email after successful booking creation
     */
    void sendBookingConfirmationEmail(Booking booking, Flight flight);

    /**
     * Send booking cancellation email when booking is cancelled
     */
    void sendBookingCancellationEmail(Booking booking, Flight flight, String cancellationReason);

}
