package com.booking.booking_service.dto.respone;

import com.booking.booking_service.entity.Booking;
import com.booking.booking_service.entity.ContactInfo;
import com.booking.booking_service.entity.FlightInfo;
import com.booking.booking_service.entity.PassengerInfo;
import com.booking.booking_service.utils.BookingStatus;
import com.booking.booking_service.utils.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class BookingResponse {
    private String id;
    private String bookingReference;
    private Long userId;
    private String flightId;
    private FlightInfo flightInfo;
    private List<PassengerInfo> passengers;
    private List<String> selectedSeats;
    private String seatClass;
    private BigDecimal totalAmount;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    private ContactInfo contactInfo;
    private String specialRequests;
    private LocalDateTime lockExpiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
