package com.booking.booking_service.service.Impl;

import com.booking.booking_service.dto.request.CreateFlightRequest;
import com.booking.booking_service.dto.request.FlightSearchRequest;
import com.booking.booking_service.dto.request.UpdateFlightRequest;
import com.booking.booking_service.dto.respone.FlightResponse;
import com.booking.booking_service.dto.respone.SeatMapResponse;
import com.booking.booking_service.entity.*;
import com.booking.booking_service.repository.FlightRepository;
import com.booking.booking_service.repository.SeatLockRepository;
import com.booking.booking_service.security.UserPrincipal;
import com.booking.booking_service.service.FlightService;
import com.booking.booking_service.utils.SeatStatus;
import com.booking.common_library.dto.PageResponse;
import com.booking.common_library.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final SeatLockRepository seatLockRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FLIGHT_CACHE_PREFIX = "flight:";
    private static final String SEARCH_CACHE_PREFIX = "search:";

    // ===== CRUD Operations =====
    @Override
    public FlightResponse createFlight(CreateFlightRequest request) {
        log.info("Creating new flight with number: {}", request.getFlightNumber());
        
        // Check if flight number already exists
        if (flightRepository.findByFlightNumber(request.getFlightNumber()).isPresent()) {
            throw new IllegalArgumentException("Flight number already exists: " + request.getFlightNumber());
        }

        // Convert DTO to entity
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setAirlineCode(request.getAirlineCode());
        flight.setAirlineName(request.getAirlineName());
        flight.setDepartureAirport(request.getDepartureAirport());
        flight.setDepartureCity(request.getDepartureCity());
        flight.setArrivalAirport(request.getArrivalAirport());
        flight.setArrivalCity(request.getArrivalCity());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setDurationMinutes(request.getDurationMinutes());
        flight.setAircraftType(request.getAircraftType());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getAvailableSeats());
        flight.setBasePrice(request.getBasePrice());
        flight.setBusinessPrice(request.getBusinessPrice());
        flight.setFirstPrice(request.getFirstPrice());
        flight.setStatus(request.getStatus());
        flight.setCreatedAt(LocalDateTime.now());
        flight.setUpdatedAt(LocalDateTime.now());

        // Convert seat configuration if provided
        if (request.getSeatConfiguration() != null) {
            List<SeatConfiguration> seatConfigs = request.getSeatConfiguration().stream()
                    .map(config -> {
                        SeatConfiguration seatConfig = new SeatConfiguration();
                        seatConfig.setSeatClass(config.getSeatClass());
                        seatConfig.setTotalSeats(config.getTotalSeats());
                        seatConfig.setAvailableSeats(config.getAvailableSeats());
                        seatConfig.setPrice(config.getPrice());
                        return seatConfig;
                    })
                    .toList();
            flight.setSeatConfiguration(seatConfigs);
        }

        // Save flight
        Flight savedFlight = flightRepository.save(flight);
        
        // Clear search cache
        clearSearchCache();
        
        return convertToFlightResponse(savedFlight);
    }

    @Override
    public FlightResponse updateFlight(String flightId, UpdateFlightRequest request) {
        log.info("Updating flight with ID: {}", flightId);
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Update fields if provided
        if (request.getFlightNumber() != null) {
            // Check if new flight number already exists (excluding current flight)
            flightRepository.findByFlightNumber(request.getFlightNumber())
                    .filter(existingFlight -> !existingFlight.getId().equals(flightId))
                    .ifPresent(existingFlight -> {
                        throw new IllegalArgumentException("Flight number already exists: " + request.getFlightNumber());
                    });
            flight.setFlightNumber(request.getFlightNumber());
        }
        
        if (request.getAirlineCode() != null) flight.setAirlineCode(request.getAirlineCode());
        if (request.getAirlineName() != null) flight.setAirlineName(request.getAirlineName());
        if (request.getDepartureAirport() != null) flight.setDepartureAirport(request.getDepartureAirport());
        if (request.getDepartureCity() != null) flight.setDepartureCity(request.getDepartureCity());
        if (request.getArrivalAirport() != null) flight.setArrivalAirport(request.getArrivalAirport());
        if (request.getArrivalCity() != null) flight.setArrivalCity(request.getArrivalCity());
        if (request.getDepartureTime() != null) flight.setDepartureTime(request.getDepartureTime());
        if (request.getArrivalTime() != null) flight.setArrivalTime(request.getArrivalTime());
        if (request.getDurationMinutes() != null) flight.setDurationMinutes(request.getDurationMinutes());
        if (request.getAircraftType() != null) flight.setAircraftType(request.getAircraftType());
        if (request.getTotalSeats() != null) flight.setTotalSeats(request.getTotalSeats());
        if (request.getAvailableSeats() != null) flight.setAvailableSeats(request.getAvailableSeats());
        if (request.getBasePrice() != null) flight.setBasePrice(request.getBasePrice());
        if (request.getBusinessPrice() != null) flight.setBusinessPrice(request.getBusinessPrice());
        if (request.getFirstPrice() != null) flight.setFirstPrice(request.getFirstPrice());
        if (request.getStatus() != null) flight.setStatus(request.getStatus());
        
        // Update seat configuration if provided
        if (request.getSeatConfiguration() != null) {
            List<SeatConfiguration> seatConfigs = request.getSeatConfiguration().stream()
                    .map(config -> {
                        SeatConfiguration seatConfig = new SeatConfiguration();
                        seatConfig.setSeatClass(config.getSeatClass());
                        seatConfig.setTotalSeats(config.getTotalSeats());
                        seatConfig.setAvailableSeats(config.getAvailableSeats());
                        seatConfig.setPrice(config.getPrice());
                        return seatConfig;
                    })
                    .toList();
            flight.setSeatConfiguration(seatConfigs);
        }
        
        flight.setUpdatedAt(LocalDateTime.now());

        // Save updated flight
        Flight updatedFlight = flightRepository.save(flight);
        
        // Clear caches
        clearFlightCache(flightId);
        clearSearchCache();
        
        return convertToFlightResponse(updatedFlight);
    }

    @Override
    public void deleteFlight(String flightId) {
        log.info("Deleting flight with ID: {}", flightId);
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Check if flight can be deleted (e.g., no active bookings)
        // This is a placeholder - you might want to add business logic here
        if (flight.getAvailableSeats() < flight.getTotalSeats()) {
            throw new IllegalStateException("Cannot delete flight with active bookings");
        }

        flightRepository.deleteById(flightId);
        
        // Clear caches
        clearFlightCache(flightId);
        clearSearchCache();
    }

    @Override
    public PageResponse<FlightResponse> getAllFlights(Pageable pageable) {
        log.info("Getting all flights with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Flight> flightPage = flightRepository.findAll(pageable);
        
        List<FlightResponse> flightResponses = flightPage.getContent().stream()
                .map(this::convertToFlightResponse)
                .toList();

        return PageResponse.<FlightResponse>builder()
                .content(flightResponses)
                .page(flightPage.getNumber())
                .size(flightPage.getSize())
                .totalElements(flightPage.getTotalElements())
                .totalPages(flightPage.getTotalPages())
                .first(flightPage.isFirst())
                .last(flightPage.isLast())
                .build();
    }

    // ===== Existing methods =====
    @Override
    public PageResponse<FlightResponse> searchFlights(FlightSearchRequest request, Pageable pageable) {
        log.info("Searching flights from {} to {} on {}",
                request.getDepartureAirport(), request.getArrivalAirport(), request.getDepartureDate());

        // Tạo cache key cho search results
        String cacheKey = SEARCH_CACHE_PREFIX + request.hashCode() + ":" + pageable.toString();

        // Kiểm tra cache trước
        @SuppressWarnings("unchecked")
        PageResponse<FlightResponse> cachedResult = (PageResponse<FlightResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            log.debug("Returning cached search results");
            return cachedResult;
        }

        // Tính toán thời gian tìm kiếm
        LocalDateTime startDate = request.getDepartureDate().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(1);

        // Tạo sort criteria
        Sort sort = createSortCriteria(request.getSortBy(), request.getSortOrder());
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // Tìm kiếm trong database
        Page<Flight> flightPage = flightRepository.findAvailableFlightsByRoute(
                request.getDepartureAirport(),
                request.getArrivalAirport(),
                startDate,
                endDate,
                sortedPageable
        );

        // Convert to response DTOs
        List<FlightResponse> flightResponses = flightPage.getContent().stream()
                .map(this::convertToFlightResponse)
                .toList();

        PageResponse<FlightResponse> result = PageResponse.<FlightResponse>builder()
                .content(flightResponses)
                .page(flightPage.getNumber())
                .size(flightPage.getSize())
                .totalElements(flightPage.getTotalElements())
                .totalPages(flightPage.getTotalPages())
                .first(flightPage.isFirst())
                .last(flightPage.isLast())
                .build();

        // Cache kết quả trong 5 phút
        redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public FlightResponse getFlightById(String flightId) {
        log.info("Getting flight details for ID: {}", flightId);

        // Kiểm tra cache trước
        String cacheKey = FLIGHT_CACHE_PREFIX + flightId;
        FlightResponse cachedFlight = (FlightResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cachedFlight != null) {
            return cachedFlight;
        }

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        FlightResponse response = convertToFlightResponse(flight);

        // Cache trong 10 phút
        redisTemplate.opsForValue().set(cacheKey, response, 10, TimeUnit.MINUTES);

        return response;
    }

    @Override
    public SeatMapResponse getFlightSeatMap(String flightId) {
        log.info("Getting seat map for flight: {}", flightId);

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Lấy thông tin user hiện tại (nếu có)
        Long currentUserId = getCurrentUserId();

        // Generate seat map
        return generateSeatMap(flight, currentUserId);
    }

    @Override
    public boolean isSeatsAvailable(String flightId, List<String> seatNumbers) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Kiểm tra ghế có tồn tại không
        List<String> allSeats = generateAllSeatNumbers(flight);
        for (String seatNumber : seatNumbers) {
            if (!allSeats.contains(seatNumber)) {
                return false;
            }
        }

        // Kiểm tra ghế có bị lock hoặc đã book không
        for (String seatNumber : seatNumbers) {
            if (isSeatOccupied(flightId, seatNumber)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateAvailableSeats(String flightId, int seatCount, boolean increase) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        if (increase) {
            flight.setAvailableSeats(flight.getAvailableSeats() + seatCount);
        } else {
            flight.setAvailableSeats(flight.getAvailableSeats() - seatCount);
        }

        flightRepository.save(flight);

        // Invalidate cache
        redisTemplate.delete(FLIGHT_CACHE_PREFIX + flightId);
    }

    // ===== Helper methods =====
    private Sort createSortCriteria(String sortBy, String sortOrder) {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return switch (sortBy.toUpperCase()) {
            case "PRICE" -> Sort.by(direction, "basePrice");
            case "DEPARTURE_TIME" -> Sort.by(direction, "departureTime");
            case "DURATION" -> Sort.by(direction, "durationMinutes");
            default -> Sort.by(direction, "departureTime");
        };
    }

    private FlightResponse convertToFlightResponse(Flight flight) {
        FlightResponse response = new FlightResponse();
        response.setId(flight.getId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setAirlineCode(flight.getAirlineCode());
        response.setAirlineName(flight.getAirlineName());
        response.setDepartureAirport(flight.getDepartureAirport());
        response.setDepartureCity(flight.getDepartureCity());
        response.setArrivalAirport(flight.getArrivalAirport());
        response.setArrivalCity(flight.getArrivalCity());
        response.setDepartureTime(flight.getDepartureTime());
        response.setArrivalTime(flight.getArrivalTime());
        response.setDurationMinutes(flight.getDurationMinutes());
        response.setAircraftType(flight.getAircraftType());
        response.setTotalSeats(flight.getTotalSeats());
        response.setAvailableSeats(flight.getAvailableSeats());
        response.setBasePrice(flight.getBasePrice());
        response.setBusinessPrice(flight.getBusinessPrice());
        response.setFirstPrice(flight.getFirstPrice());
        response.setStatus(flight.getStatus());
        
        if (flight.getSeatConfiguration() != null) {
            List<SeatClassInfo> seatClassInfos = flight.getSeatConfiguration().stream()
                    .map(config -> {
                        SeatClassInfo seatClassInfo = new SeatClassInfo();
                        seatClassInfo.setSeatClass(String.valueOf(config.getSeatClass()));
                        seatClassInfo.setTotalSeats(config.getTotalSeats());
                        seatClassInfo.setAvailableSeats(config.getAvailableSeats());
                        seatClassInfo.setPrice(config.getPrice());
                        return seatClassInfo;
                    })
                    .toList();
            response.setSeatConfiguration(seatClassInfos);
        }
        return response;
    }

    private SeatMapResponse generateSeatMap(Flight flight, Long currentUserId) {
        List<SeatRow> seatRows = new ArrayList<>();
        int totalRows = flight.getTotalSeats() / 6;

        for (int row = 1; row <= totalRows; row++) {
            String seatClass = row <= 5 ? "BUSINESS" : "ECONOMY";
            List<Seat> seats = new ArrayList<>();
            for (char seatLetter = 'A'; seatLetter <= 'F'; seatLetter++) {
                String seatNumber = row + String.valueOf(seatLetter);
                SeatStatus status = getSeatStatus(flight.getId(), seatNumber, currentUserId);
                String seatType = (seatLetter == 'A' || seatLetter == 'F') ? "WINDOW" :
                        (seatLetter == 'C' || seatLetter == 'D') ? "AISLE" : "MIDDLE";
                seats.add(Seat.builder()
                        .seatNumber(seatNumber)
                        .seatType(seatType)
                        .status(status)
                        .extraPrice(BigDecimal.ZERO)
                        .isEmergencyExit(row == 12 || row == 13)
                        .hasExtraLegroom(row <= 5 || row == 12 || row == 13)
                        .build());
            }
            seatRows.add(SeatRow.builder()
                    .rowNumber(row)
                    .seatClass(seatClass)
                    .seats(seats)
                    .build());
        }

        SeatLegend legend = SeatLegend.builder()
                .available("Available for selection")
                .occupied("Already booked")
                .locked("Temporarily locked by another user")
                .selected("Selected by you")
                .unavailable("Not available for booking")
                .build();

        return SeatMapResponse.builder()
                .flightId(flight.getId())
                .aircraftType(flight.getAircraftType())
                .seatRows(seatRows)
                .legend(legend)
                .build();
    }

    private SeatStatus getSeatStatus(String flightId, String seatNumber, Long currentUserId) {
        if (isSeatOccupied(flightId, seatNumber)) {
            return SeatStatus.OCCUPIED;
        }
        SeatLock activeLock = seatLockRepository.findActiveLockBySeat(flightId, seatNumber).orElse(null);
        if (activeLock != null) {
            return (currentUserId != null && activeLock.getUserId().equals(currentUserId))
                    ? SeatStatus.SELECTED
                    : SeatStatus.LOCKED;
        }
        return SeatStatus.AVAILABLE;
    }

    private boolean isSeatOccupied(String flightId, String seatNumber) {
        return flightRepository.findById(flightId)
                .map(flight -> false) // Placeholder, cần tích hợp với booking
                .orElse(true);
    }

    private List<String> generateAllSeatNumbers(Flight flight) {
        List<String> allSeats = new ArrayList<>();
        int totalRows = flight.getTotalSeats() / 6;
        for (int row = 1; row <= totalRows; row++) {
            for (char seatLetter = 'A'; seatLetter <= 'F'; seatLetter++) {
                allSeats.add(row + String.valueOf(seatLetter));
            }
        }
        return allSeats;
    }

    private Long getCurrentUserId() {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userPrincipal.getId();
        } catch (Exception e) {
            return null;
        }
    }

    // ===== Helper methods for CRUD =====
    private void clearFlightCache(String flightId) {
        redisTemplate.delete(FLIGHT_CACHE_PREFIX + flightId);
    }

    private void clearSearchCache() {
        // Clear all search cache keys
        Set<String> keys = redisTemplate.keys(SEARCH_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}

