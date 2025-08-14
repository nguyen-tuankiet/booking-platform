package com.booking.booking_service.entity;

import com.booking.booking_service.utils.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    private String seatNumber;
    private String seatType; // WINDOW, MIDDLE, AISLE
    private SeatStatus status;
    private BigDecimal extraPrice;
    private Boolean isEmergencyExit;
    private Boolean hasExtraLegroom;
}