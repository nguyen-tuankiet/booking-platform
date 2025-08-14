package com.booking.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatClassInfo {
    private String seatClass;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal price;
}