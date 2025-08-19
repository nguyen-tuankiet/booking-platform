package com.booking.common_library.entity.booking_event;

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
public class BookingCreatedEvent {
    private String bookingId;
    private String bookingReference;
    private Long userId;
    private String flightId;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime createdAt;
    private String passengerEmail;
    private String passengerPhone;
}