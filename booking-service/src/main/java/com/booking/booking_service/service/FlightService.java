package com.booking.booking_service.service;

import com.booking.booking_service.dto.request.CreateFlightRequest;
import com.booking.booking_service.dto.request.FlightSearchRequest;
import com.booking.booking_service.dto.request.UpdateFlightRequest;
import com.booking.booking_service.dto.respone.FlightResponse;
import com.booking.booking_service.dto.respone.SeatMapResponse;
import com.booking.common_library.dto.PageResponse;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FlightService {
    // CRUD Operations
    FlightResponse createFlight(CreateFlightRequest request);
    FlightResponse updateFlight(String flightId, UpdateFlightRequest request);
    void deleteFlight(String flightId);
    PageResponse<FlightResponse> getAllFlights(Pageable pageable);
    
    // Existing methods
    PageResponse<FlightResponse> searchFlights(FlightSearchRequest request, Pageable pageable);
    FlightResponse getFlightById(String flightId);
    SeatMapResponse getFlightSeatMap(String flightId);
    boolean isSeatsAvailable(String flightId, List<String> seatNumbers);
    void updateAvailableSeats(String flightId, int seatCount, boolean increase);
}
