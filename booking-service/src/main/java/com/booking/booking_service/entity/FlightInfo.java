package com.booking.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInfo {
    private String flightNumber;
    private String airlineName;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
}
