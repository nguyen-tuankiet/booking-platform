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
public class CreateFlightRequest {

    @NotBlank(message = "Flight number is required")
    @Pattern(regexp = "^[A-Z]{2}\\d{3,4}$", message = "Flight number must be in format: AA123 or AA1234")
    private String flightNumber;

    @NotBlank(message = "Airline code is required")
    @Size(min = 2, max = 3, message = "Airline code must be 2-3 characters")
    private String airlineCode;

    @NotBlank(message = "Airline name is required")
    private String airlineName;

    @NotBlank(message = "Departure airport is required")
    private String departureAirport;

    @NotBlank(message = "Departure city is required")
    private String departureCity;

    @NotBlank(message = "Arrival airport is required")
    private String arrivalAirport;

    @NotBlank(message = "Arrival city is required")
    private String arrivalCity;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours")
    private Integer durationMinutes;

    @NotBlank(message = "Aircraft type is required")
    private String aircraftType;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 1000, message = "Total seats cannot exceed 1000")
    private Integer totalSeats;

    @NotNull(message = "Available seats is required")
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    private BigDecimal basePrice;

    @NotNull(message = "Business price is required")
    @DecimalMin(value = "0.01", message = "Business price must be greater than 0")
    private BigDecimal businessPrice;

    @NotNull(message = "First class price is required")
    @DecimalMin(value = "0.01", message = "First class price must be greater than 0")
    private BigDecimal firstPrice;

    private List<SeatConfigurationRequest> seatConfiguration;

    @NotNull(message = "Flight status is required")
    private FlightStatus status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatConfigurationRequest {
        @NotNull(message = "Seat class is required")
        private SeatClass seatClass;

        @NotNull(message = "Total seats is required")
        @Min(value = 1, message = "Total seats must be at least 1")
        private Integer totalSeats;

        @NotNull(message = "Available seats is required")
        @Min(value = 0, message = "Available seats cannot be negative")
        private Integer availableSeats;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;
    }
}

