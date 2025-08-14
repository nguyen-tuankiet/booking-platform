package com.booking.booking_service.dto.request;

import com.booking.booking_service.utils.FlightStatus;
import com.booking.booking_service.utils.SeatClass;
import jakarta.validation.constraints.*;
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
public class UpdateFlightRequest {

    @Pattern(regexp = "^[A-Z]{2}\\d{3,4}$", message = "Flight number must be in format: AA123 or AA1234")
    private String flightNumber;

    @Size(min = 2, max = 3, message = "Airline code must be 2-3 characters")
    private String airlineCode;

    private String airlineName;

    private String departureAirport;

    private String departureCity;

    private String arrivalAirport;

    private String arrivalCity;

    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;

    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours")
    private Integer durationMinutes;

    private String aircraftType;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 1000, message = "Total seats cannot exceed 1000")
    private Integer totalSeats;

    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.01", message = "Business price must be greater than 0")
    private BigDecimal businessPrice;

    @DecimalMin(value = "0.01", message = "First class price must be greater than 0")
    private BigDecimal firstPrice;

    private List<SeatConfigurationRequest> seatConfiguration;

    private FlightStatus status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatConfigurationRequest {
        private SeatClass seatClass;

        @Min(value = 1, message = "Total seats must be at least 1")
        private Integer totalSeats;

        @Min(value = 0, message = "Available seats cannot be negative")
        private Integer availableSeats;

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;
    }
}

