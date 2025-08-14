package com.booking.booking_service.repository;

import com.booking.booking_service.entity.Flight;
import com.booking.booking_service.utils.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends MongoRepository<Flight, String> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByAirlineCode(String airlineCode);

    @Query("{ 'departureAirport': ?0, 'arrivalAirport': ?1, 'departureTime': { $gte: ?2, $lt: ?3 }, 'status': 'SCHEDULED' }")
    Page<Flight> findFlightsByRoute(String departureAirport, String arrivalAirport,
                                    LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("{ 'departureAirport': ?0, 'arrivalAirport': ?1, 'departureTime': { $gte: ?2, $lt: ?3 }, 'status': 'SCHEDULED', 'availableSeats': { $gt: 0 } }")
    Page<Flight> findAvailableFlightsByRoute(String departureAirport, String arrivalAirport,
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("{ 'departureTime': { $gte: ?0, $lt: ?1 }, 'status': 'SCHEDULED' }")
    List<Flight> findFlightsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'status': ?0 }")
    Page<Flight> findByStatus(FlightStatus status, Pageable pageable);

    @Query("{ 'departureAirport': { $regex: ?0, $options: 'i' } }")
    List<Flight> findByDepartureAirportContaining(String airport);

    @Query("{ 'arrivalAirport': { $regex: ?0, $options: 'i' } }")
    List<Flight> findByArrivalAirportContaining(String airport);
}