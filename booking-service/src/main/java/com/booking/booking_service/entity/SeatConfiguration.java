package com.booking.booking_service.entity;

import com.booking.booking_service.utils.SeatClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatConfiguration {
    private SeatClass seatClass; // ECONOMY, BUSINESS, FIRST
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal price;
}
