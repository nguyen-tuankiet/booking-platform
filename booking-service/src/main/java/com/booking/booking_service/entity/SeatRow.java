package com.booking.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class SeatRow {
    private Integer rowNumber;
    private String seatClass;
    private List<Seat> seats;
}
