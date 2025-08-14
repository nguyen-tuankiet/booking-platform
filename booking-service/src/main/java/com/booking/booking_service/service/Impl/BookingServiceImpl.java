package com.booking.booking_service.service.Impl;

import com.booking.booking_service.dto.request.BookingRequest;
import com.booking.booking_service.dto.request.ContactRequest;
import com.booking.booking_service.dto.request.PassengerRequest;
import com.booking.booking_service.dto.request.SeatSelectionRequest;
import com.booking.booking_service.dto.respone.BookingResponse;
import com.booking.booking_service.entity.*;
import com.booking.booking_service.repository.BookingRepository;
import com.booking.booking_service.repository.FlightRepository;
import com.booking.booking_service.security.UserPrincipal;
import com.booking.booking_service.service.BookingService;
import com.booking.booking_service.service.FlightService;
import com.booking.booking_service.service.SeatLockService;
import com.booking.booking_service.utils.BookingStatus;
import com.booking.booking_service.utils.FlightStatus;
import com.booking.booking_service.utils.PaymentStatus;
import com.booking.common_library.dto.PageResponse;
import com.booking.common_library.exception.BusinessException;
import com.booking.common_library.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final SeatLockService seatLockService;
    private final FlightService flightService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ModelMapper modelMapper;

    private static final String BOOKING_CACHE_PREFIX = "booking:";
    private static final String USER_BOOKINGS_PREFIX = "user_bookings:";

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Long userId = getCurrentUserId();
        log.info("Creating booking for user {} on flight {}", userId, request.getFlightId());

        // Validate flight existence và availability
        Flight flight = validateFlightForBooking(request.getFlightId());

        // Validate seat selection
        validateSeatSelection(request);

        // Kiểm tra user có đang lock các ghế này không
        validateUserSeatLocks(request.getFlightId(), request.getSelectedSeats(), userId);

        // Tính tổng tiền
        BigDecimal totalAmount = calculateTotalAmount(flight, request);

        // Tạo booking reference
        String bookingReference = generateBookingReference();

        // Tạo booking entity
        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .userId(userId)
                .flightId(request.getFlightId())
                .passengers(convertPassengers(request.getPassengers()))
                .selectedSeats(request.getSelectedSeats())
                .seatClass(request.getSeatClass())
                .totalAmount(totalAmount)
                .bookingStatus(BookingStatus.LOCKED)
                .paymentStatus(PaymentStatus.PENDING)
                .contactInfo(convertContactInfo(request.getContactInfo()))
                .specialRequests(request.getSpecialRequests())
                .lockExpiresAt(LocalDateTime.now().plusMinutes(15)) // 15 phút để thanh toán
                .build();

        booking = bookingRepository.save(booking);

        // Confirm seat locks với booking ID
        seatLockService.confirmSeatLocks(request.getFlightId(), request.getSelectedSeats(), userId, booking.getId());

        // Invalidate cache
        invalidateUserBookingsCache(userId);

        log.info("Successfully created booking {} for user {}", bookingReference, userId);
        return convertToBookingResponse(booking, flight);
    }

    @Override
    @Transactional
    public List<SeatLock> selectSeats(SeatSelectionRequest request) {
        Long userId = getCurrentUserId();
        log.info("User {} selecting seats {} for flight {}", userId, request.getSeatNumbers(), request.getFlightId());

        // Validate flight
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", request.getFlightId()));

        // Validate seat availability
        if (!flightService.isSeatsAvailable(request.getFlightId(), request.getSeatNumbers())) {
            throw new BusinessException("One or more selected seats are not available");
        }

        // Release existing locks của user trên flight này
        seatLockService.releaseUserLocks(request.getFlightId(), userId);

        // Lock ghế mới
        return seatLockService.lockSeats(request.getFlightId(), request.getSeatNumbers(),
                userId, request.getSessionId());
    }

    @Override
    @Transactional
    public PageResponse<BookingResponse> getUserBookings(Pageable pageable) {
        Long userId = getCurrentUserId();
        log.info("Getting bookings for user: {}", userId);

        // Kiểm tra cache
        String cacheKey = USER_BOOKINGS_PREFIX + userId + ":" + pageable.toString();
        @SuppressWarnings("unchecked")
        PageResponse<BookingResponse> cachedResult = (PageResponse<BookingResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);

        List<BookingResponse> bookingResponses = bookingPage.getContent().stream()
                .map(booking -> {
                    Flight flight = flightRepository.findById(booking.getFlightId()).orElse(null);
                    return convertToBookingResponse(booking, flight);
                })
                .toList();

        PageResponse<BookingResponse> result = PageResponse.<BookingResponse>builder()
                .content(bookingResponses)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .first(bookingPage.isFirst())
                .last(bookingPage.isLast())
                .build();

        // Cache 5 phút
        redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public BookingResponse getBookingByReference(String bookingReference) {
        log.info("Getting booking by reference: {}", bookingReference);

        String cacheKey = BOOKING_CACHE_PREFIX + "ref:" + bookingReference;
        BookingResponse cachedBooking = (BookingResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cachedBooking != null) {
            return cachedBooking;
        }

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));

        // Kiểm tra quyền truy cập
        Long currentUserId = getCurrentUserId();
        if (!booking.getUserId().equals(currentUserId) && !isAdmin()) {
            throw new BusinessException("Access denied to this booking");
        }

        Flight flight = flightRepository.findById(booking.getFlightId()).orElse(null);
        BookingResponse response = convertToBookingResponse(booking, flight);

        // Cache 10 phút
        redisTemplate.opsForValue().set(cacheKey, response, 10, TimeUnit.MINUTES);
        return response;
    }

    @Override
    @Transactional
    public void cancelBooking(String bookingId, String reason) {
        Long userId = getCurrentUserId();
        log.info("User {} cancelling booking {}", userId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Kiểm tra quyền hủy
        if (!booking.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException("Access denied to cancel this booking");
        }

        // Kiểm tra trạng thái có thể hủy
        if (booking.getBookingStatus() == BookingStatus.CANCELLED ||
                booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel booking in current status");
        }

        // Update booking status
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        // Release seats về available pool
        flightService.updateAvailableSeats(booking.getFlightId(), booking.getSelectedSeats().size(), true);

        // Invalidate caches
        invalidateBookingCaches(booking);

        log.info("Successfully cancelled booking {}", bookingId);
    }

    @Override
    @Transactional
    public void confirmBooking(String bookingId) {
        log.info("Confirming booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getBookingStatus() != BookingStatus.LOCKED) {
            throw new BusinessException("Cannot confirm booking in current status");
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setConfirmedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        assignSeatsToPassengers(booking);
        bookingRepository.save(booking);
        flightService.updateAvailableSeats(booking.getFlightId(), booking.getSelectedSeats().size(), false);
        invalidateBookingCaches(booking);

        log.info("Successfully confirmed booking {}", bookingId);
    }

    @Override
    @Transactional
    public void expireBooking(String bookingId) {
        log.info("Expiring booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        booking.setBookingStatus(BookingStatus.EXPIRED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        seatLockService.releaseUserLocks(booking.getFlightId(), booking.getUserId());
        invalidateBookingCaches(booking);
    }

    @Override
    public BookingResponse getBookingById(String bookingId) {
        Long userId = getCurrentUserId();
        log.info("Getting booking {} for user {}", bookingId, userId);

        String cacheKey = BOOKING_CACHE_PREFIX + bookingId;
        BookingResponse cachedBooking = (BookingResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cachedBooking != null) return cachedBooking;

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getUserId().equals(userId) && !isAdmin()) {
            throw new BusinessException("Access denied to this booking");
        }

        Flight flight = flightRepository.findById(booking.getFlightId()).orElse(null);
        BookingResponse response = convertToBookingResponse(booking, flight);

        redisTemplate.opsForValue().set(cacheKey, response, 10, TimeUnit.MINUTES);
        return response;
    }

    @Override
    @Transactional
    public void updateBookingPaymentStatus(String bookingId, PaymentStatus paymentStatus) {
        log.info("Updating payment status for booking {} to {}", bookingId, paymentStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        booking.setPaymentStatus(paymentStatus);
        booking.setUpdatedAt(LocalDateTime.now());

        if (paymentStatus == PaymentStatus.COMPLETED) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setConfirmedAt(LocalDateTime.now());
            assignSeatsToPassengers(booking);
        } else if (paymentStatus == PaymentStatus.FAILED) {
            booking.setBookingStatus(BookingStatus.EXPIRED);
            seatLockService.releaseUserLocks(booking.getFlightId(), booking.getUserId());
        }

        bookingRepository.save(booking);
        invalidateBookingCaches(booking);
    }

    // === Private helper methods ===
    private Flight validateFlightForBooking(String flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new BusinessException("Flight is not available for booking");
        }

        if (flight.getAvailableSeats() <= 0) {
            throw new BusinessException("No available seats on this flight");
        }

        return flight;
    }

    private void validateSeatSelection(BookingRequest request) {
        if (request.getPassengers().size() != request.getSelectedSeats().size()) {
            throw new BusinessException("Number of passengers must match number of selected seats");
        }
    }

    private void validateUserSeatLocks(String flightId, List<String> seatNumbers, Long userId) {
        List<String> userLockedSeats = seatLockService.getUserLockedSeats(flightId, userId);
        for (String seatNumber : seatNumbers) {
            if (!userLockedSeats.contains(seatNumber)) {
                throw new BusinessException("Seat " + seatNumber + " is not locked by user");
            }
        }
    }

    private BigDecimal calculateTotalAmount(Flight flight, BookingRequest request) {
        BigDecimal seatPrice = switch (request.getSeatClass().toUpperCase()) {
            case "BUSINESS" -> flight.getBusinessPrice();
            case "FIRST" -> flight.getFirstPrice();
            default -> flight.getBasePrice();
        };
        return seatPrice.multiply(BigDecimal.valueOf(request.getPassengers().size()));
    }

    private String generateBookingReference() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private List<PassengerInfo> convertPassengers(List<PassengerRequest> passengerRequests) {
        return passengerRequests.stream()
                .map(request -> PassengerInfo.builder()
                        .title(request.getTitle())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .dateOfBirth(request.getDateOfBirth())
                        .nationality(request.getNationality())
                        .passportNumber(request.getPassportNumber())
                        .passportExpiry(request.getPassportExpiry())
                        .meal(request.getMeal())
                        .build())
                .toList();
    }

    private ContactInfo convertContactInfo(ContactRequest contactRequest) {
        return ContactInfo.builder()
                .email(contactRequest.getEmail())
                .phoneNumber(contactRequest.getPhoneNumber())
                .emergencyContact(contactRequest.getEmergencyContact())
                .emergencyPhone(contactRequest.getEmergencyPhone())
                .build();
    }

    private BookingResponse convertToBookingResponse(Booking booking, Flight flight) {
        BookingResponse response = modelMapper.map(booking, BookingResponse.class);
        if (flight != null) {
            FlightInfo flightInfo = FlightInfo.builder()
                    .flightNumber(flight.getFlightNumber())
                    .airlineName(flight.getAirlineName())
                    .departureAirport(flight.getDepartureAirport())
                    .arrivalAirport(flight.getArrivalAirport())
                    .departureTime(flight.getDepartureTime())
                    .arrivalTime(flight.getArrivalTime())
                    .build();
            response.setFlightInfo(flightInfo);
        }
        if (booking.getPassengers() != null) {
            List<PassengerInfo> passengers = booking.getPassengers().stream()
                    .map(passenger -> PassengerInfo.builder()
                            .title(passenger.getTitle())
                            .firstName(passenger.getFirstName())
                            .lastName(passenger.getLastName())
                            .dateOfBirth(passenger.getDateOfBirth())
                            .nationality(passenger.getNationality())
                            .passportNumber(passenger.getPassportNumber())
                            .passportExpiry(passenger.getPassportExpiry())
                            .seatNumber(passenger.getSeatNumber())
                            .meal(passenger.getMeal())
                            .build())
                    .toList();
            response.setPassengers(passengers);
        }
        if (booking.getContactInfo() != null) {
            ContactInfo contactInfo = ContactInfo.builder()
                    .email(booking.getContactInfo().getEmail())
                    .phoneNumber(booking.getContactInfo().getPhoneNumber())
                    .emergencyContact(booking.getContactInfo().getEmergencyContact())
                    .emergencyPhone(booking.getContactInfo().getEmergencyPhone())
                    .build();
            response.setContactInfo(contactInfo);
        }
        return response;
    }

    private void assignSeatsToPassengers(Booking booking) {
        List<PassengerInfo> passengers = booking.getPassengers();
        List<String> selectedSeats = booking.getSelectedSeats();
        for (int i = 0; i < passengers.size() && i < selectedSeats.size(); i++) {
            passengers.get(i).setSeatNumber(selectedSeats.get(i));
        }
        booking.setPassengers(passengers);
    }

    private void invalidateBookingCaches(Booking booking) {
        redisTemplate.delete(BOOKING_CACHE_PREFIX + booking.getId());
        redisTemplate.delete(BOOKING_CACHE_PREFIX + "ref:" + booking.getBookingReference());
        invalidateUserBookingsCache(booking.getUserId());
    }

    private void invalidateUserBookingsCache(Long userId) {
        String pattern = USER_BOOKINGS_PREFIX + userId + ":*";
        redisTemplate.delete(pattern);
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

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

}
