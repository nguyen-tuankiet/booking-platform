package com.booking.booking_service.controller;

import com.booking.booking_service.dto.request.CreateFlightRequest;
import com.booking.booking_service.dto.request.FlightSearchRequest;
import com.booking.booking_service.dto.request.UpdateFlightRequest;
import com.booking.booking_service.dto.respone.FlightResponse;
import com.booking.booking_service.dto.respone.SeatMapResponse;
import com.booking.booking_service.service.FlightService;
import com.booking.common_library.dto.ApiResponse;
import com.booking.common_library.dto.PageResponse;
import com.booking.common_library.util.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Management", description = "Flight search and information endpoints")
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Search for available flights based on criteria")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flights found successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search criteria")
    })
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> searchFlights(
            @Valid @ModelAttribute FlightSearchRequest request,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<FlightResponse> flights = flightService.searchFlights(request, pageable);

        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, flights));
    }

    @GetMapping("/{flightId}")
    @Operation(summary = "Get flight details", description = "Retrieve detailed information about a specific flight")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(
            @Parameter(description = "Flight ID") @PathVariable String flightId) {

        FlightResponse flight = flightService.getFlightById(flightId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, flight));
    }

    @GetMapping("/{flightId}/seats")
    @Operation(summary = "Get flight seat map", description = "Retrieve seat map and availability for a specific flight")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seat map retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<SeatMapResponse>> getFlightSeatMap(
            @Parameter(description = "Flight ID") @PathVariable String flightId) {

        SeatMapResponse seatMap = flightService.getFlightSeatMap(flightId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, seatMap));
    }

    @GetMapping("/{flightId}/availability")
    @Operation(summary = "Check seat availability", description = "Check if specific seats are available for booking")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Seat availability checked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found")
    })
    public ResponseEntity<ApiResponse<Boolean>> checkSeatAvailability(
            @Parameter(description = "Flight ID") @PathVariable String flightId,
            @Parameter(description = "Seat numbers to check") @RequestParam List<String> seatNumbers) {

        boolean available = flightService.isSeatsAvailable(flightId, seatNumbers);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, available));
    }

    // ===== CRUD Operations =====
    
    @PostMapping
    @Operation(summary = "Create new flight", description = "Create a new flight with all required information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Flight created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid flight data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Flight number already exists")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
            @Valid @RequestBody CreateFlightRequest request) {

        FlightResponse flight = flightService.createFlight(request);
        return ResponseEntity.status(201).body(ApiResponse.builderResponse(SuccessCode.CREATED, flight));
    }

    @PutMapping("/{flightId}")
    @Operation(summary = "Update flight", description = "Update an existing flight's information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid flight data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Flight number already exists")
    })
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @Parameter(description = "Flight ID") @PathVariable String flightId,
            @Valid @RequestBody UpdateFlightRequest request) {

        FlightResponse flight = flightService.updateFlight(flightId, request);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.UPDATED, flight));
    }

    @DeleteMapping("/{flightId}")
    @Operation(summary = "Delete flight", description = "Delete a flight (only if no active bookings)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flight deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Flight not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot delete flight with active bookings")
    })
    public ResponseEntity<ApiResponse<Void>> deleteFlight(
            @Parameter(description = "Flight ID") @PathVariable String flightId) {

        flightService.deleteFlight(flightId);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.DELETED, null));
    }

    @GetMapping
    @Operation(summary = "Get all flights", description = "Retrieve all flights with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
    })
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> getAllFlights(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<FlightResponse> flights = flightService.getAllFlights(pageable);
        return ResponseEntity.ok(ApiResponse.builderResponse(SuccessCode.FETCHED, flights));
    }
}