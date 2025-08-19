package com.booking.booking_service.service;

import com.booking.common_library.entity.booking_event.*;

public interface BookingEventPublisher {
    void publishBookingCreated(BookingCreatedEvent event);
    void publishBookingConfirmed(BookingConfirmedEvent event);
    void publishBookingCancelled(BookingCancelledEvent event);
    void publishBookingExpired(BookingExpiredEvent event);
    void publishPaymentRequested(PaymentRequestedEvent event);
}
