package com.booking.booking_service.dto.respone;

import com.booking.booking_service.entity.Flight;
import com.booking.booking_service.entity.SeatClassInfo;
import com.booking.booking_service.utils.FlightStatus;
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
public class FlightResponse {
    private String id;
    private String flightNumber;
    private String airlineCode;
    private String airlineName;
    private String departureAirport;
    private String departureCity;
    private String arrivalAirport;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private String aircraftType;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal basePrice;
    private BigDecimal businessPrice;
    private BigDecimal firstPrice;
    private List<SeatClassInfo> seatConfiguration;
    private FlightStatus status;

}

