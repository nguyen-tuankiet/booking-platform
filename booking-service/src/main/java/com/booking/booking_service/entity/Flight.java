package com.booking.booking_service.entity;

import com.booking.booking_service.utils.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("flights")
public class Flight {

    @Id
    private String id;

    @Field("flight_number")
    private String flightNumber;

    @Field("airline_code")
    private String airlineCode;

    @Field("airline_name")
    private String airlineName;

    @Field("departure_airport")
    private String departureAirport;

    @Field("departure_city")
    private String departureCity;

    @Field("arrival_airport")
    private String arrivalAirport;

    @Field("arrival_city")
    private String arrivalCity;

    @Field("departure_time")
    private LocalDateTime departureTime;

    @Field("arrival_time")
    private LocalDateTime arrivalTime;

    @Field("duration_minutes")
    private Integer durationMinutes;

    @Field("aircraft_type")
    private String aircraftType;

    @Field("total_seats")
    private Integer totalSeats;

    @Field("available_seats")
    private Integer availableSeats;

    @Field("base_price")
    private BigDecimal basePrice;

    @Field("business_price")
    private BigDecimal businessPrice;

    @Field("first_price")
    private BigDecimal firstPrice;

    @Field("seat_configuration")
    private List<SeatConfiguration> seatConfiguration;

    @Field("status")
    private FlightStatus status;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
