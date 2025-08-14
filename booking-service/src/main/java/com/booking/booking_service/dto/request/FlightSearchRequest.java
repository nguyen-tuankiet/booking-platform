package com.booking.booking_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    @NotBlank(message = "Departure airport is required")
    private String departureAirport;

    @NotBlank(message = "Arrival airport is required")
    private String arrivalAirport;

    @NotNull(message = "Departure date is required")
    private LocalDate departureDate;

    private LocalDate returnDate; // For round trip

    @Min(value = 1, message = "At least 1 adult passenger is required")
    private Integer adults = 1;

    @Min(value = 0, message = "Children count cannot be negative")
    private Integer children = 0;

    @Min(value = 0, message = "Infants count cannot be negative")
    private Integer infants = 0;

    private String seatClass = "ECONOMY"; // ECONOMY, BUSINESS, FIRST

    private String tripType = "ONE_WAY"; // ONE_WAY, ROUND_TRIP

    private Boolean directFlightsOnly = false;

    private String sortBy = "PRICE"; // PRICE, DEPARTURE_TIME, DURATION

    private String sortOrder = "ASC"; // ASC, DESC
}
