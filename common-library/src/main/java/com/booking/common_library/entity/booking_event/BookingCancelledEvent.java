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
public class BookingCancelledEvent {
    private String bookingId;
    private String bookingReference;
    private Long userId;
    private String flightId;
    private List<String> seatNumbers;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private boolean refundRequired;
    private String transactionId;
}
