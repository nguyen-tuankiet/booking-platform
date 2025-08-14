package com.booking.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLegend {
    private String available;
    private String occupied;
    private String locked;
    private String selected;
    private String unavailable;
}
