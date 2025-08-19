package com.booking.booking_service.service.Impl;

import com.booking.booking_service.dto.request.BookingRequest;
import com.booking.booking_service.dto.request.SeatSelectionRequest;
import com.booking.booking_service.dto.request.PassengerRequest;
import com.booking.booking_service.dto.respone.BookingResponse;
import com.booking.booking_service.entity.Booking;
import com.booking.booking_service.entity.Flight;
import com.booking.booking_service.entity.SeatLock;
import com.booking.booking_service.entity.PassengerInfo;
import com.booking.booking_service.repository.BookingRepository;
import com.booking.booking_service.service.*;
import com.booking.booking_service.utils.BookingStatus;
import com.booking.booking_service.utils.PaymentStatus;
import com.booking.common_library.dto.PageResponse;
import com.booking.common_library.entity.booking_event.BookingCancelledEvent;
import com.booking.common_library.entity.booking_event.BookingConfirmedEvent;
import com.booking.common_library.entity.booking_event.BookingCreatedEvent;
import com.booking.common_library.entity.booking_event.BookingExpiredEvent;
import com.booking.common_library.exception.ResourceNotFoundException;
import com.booking.common_library.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightService flightService;
    private final SeatLockService seatLockService;
    private final EmailNotificationService emailNotificationService;
    private final BookingEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for flight: {} with {} passengers",
                request.getFlightId(), request.getPassengers().size());

        try {
            // Validate flight exists and is available
            var flightResponse = flightService.getFlightById(request.getFlightId());

            // Check seat availability
            if (!flightService.isSeatsAvailable(request.getFlightId(), request.getSelectedSeats())) {
                throw new BusinessException("Selected seats are not available");
            }

            // Get current user
            Long userId = getCurrentUserId();

            // Check if user already has locked seats for this flight
            if (seatLockService.hasUserLockedSeats(request.getFlightId(), userId)) {
                List<String> lockedSeats = seatLockService.getUserLockedSeats(request.getFlightId(), userId);
                log.warn("User {} already has locked seats {} for flight {}", userId, lockedSeats, request.getFlightId());
                throw new BusinessException("You already have seats locked for this flight. Please complete or cancel your existing booking.");
            }

            // Calculate total amount based on seat class and number of seats
            BigDecimal totalAmount = calculateTotalAmount(flightResponse, request.getSeatClass(), request.getSelectedSeats().size());

            // Map contact info
            var contactInfo = com.booking.booking_service.entity.ContactInfo.builder()
                    .email(request.getContactInfo().getEmail())
                    .phoneNumber(request.getContactInfo().getPhoneNumber())
                    .emergencyContact(request.getContactInfo().getEmergencyContact())
                    .emergencyPhone(request.getContactInfo().getEmergencyPhone())
                    .build();

            // Create booking entity
            Booking booking = Booking.builder()
                    .id(generateBookingId())
                    .bookingReference(generateBookingReference())
                    .userId(userId)
                    .flightId(request.getFlightId())
                    .passengers(mapPassengers(request.getPassengers()))
                    .selectedSeats(request.getSelectedSeats())
                    .seatClass(request.getSeatClass())
                    .totalAmount(totalAmount)
                    .bookingStatus(BookingStatus.LOCKED)
                    .paymentStatus(PaymentStatus.PENDING)
                    .contactInfo(contactInfo)
                    .specialRequests(request.getSpecialRequests())
                    .lockExpiresAt(LocalDateTime.now().plusMinutes(15))
                    .build();

            // Save booking
            booking = bookingRepository.save(booking);
            log.info("Booking created with ID: {} and reference: {}", booking.getId(), booking.getBookingReference());

            // Lock seats
            String sessionId = UUID.randomUUID().toString();
            List<SeatLock> seatLocks = seatLockService.lockSeats(
                    request.getFlightId(),
                    request.getSelectedSeats(),
                    userId,
                    sessionId
            );
            log.info("Locked {} seats for booking: {}", seatLocks.size(), booking.getId());

            // Update flight available seats
            flightService.updateAvailableSeats(request.getFlightId(), request.getSelectedSeats().size(), false);

            // Publish booking created event
            BookingCreatedEvent event = BookingCreatedEvent.builder()
                    .bookingId(booking.getId())
                    .bookingReference(booking.getBookingReference())
                    .userId(booking.getUserId())
                    .flightId(booking.getFlightId())
                    .seatNumbers(booking.getSelectedSeats())
                    .totalAmount(booking.getTotalAmount())
                    .currency("VND")
                    .createdAt(booking.getCreatedAt())
                    .passengerEmail(booking.getContactInfo() != null ? booking.getContactInfo().getEmail() : null)
                    .passengerPhone(booking.getContactInfo() != null ? booking.getContactInfo().getPhoneNumber() : null)
                    .build();

            eventPublisher.publishBookingCreated(event);

            // Send confirmation email
            try {
                Flight flight = convertToFlightEntity(flightResponse);
                emailNotificationService.sendBookingConfirmationEmail(booking, flight);
            } catch (Exception e) {
                log.error("Failed to send booking confirmation email", e);
                // Don't fail the booking creation if email fails
            }

            log.info("Booking created successfully: {}", booking.getId());

            return convertToBookingResponse(booking);

        } catch (Exception e) {
            log.error("Failed to create booking for flight: {}", request.getFlightId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<SeatLock> selectSeats(SeatSelectionRequest request) {
        log.info("Selecting seats {} for flight: {}", request.getSeatNumbers(), request.getFlightId());

        // Validate flight exists
        flightService.getFlightById(request.getFlightId());

        // Check seat availability
        if (!flightService.isSeatsAvailable(request.getFlightId(), request.getSeatNumbers())) {
            throw new BusinessException("One or more selected seats are not available");
        }

        Long userId = getCurrentUserId();
        String sessionId = UUID.randomUUID().toString();

        // Release any existing locks for this user on this flight
        seatLockService.releaseUserLocks(request.getFlightId(), userId);

        // Lock the new seats
        return seatLockService.lockSeats(request.getFlightId(), request.getSeatNumbers(), userId, sessionId);
    }

    @Override
    public PageResponse<BookingResponse> getUserBookings(Pageable pageable) {
        Long userId = getCurrentUserId();
        log.info("Getting bookings for user: {}", userId);

        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);

        List<BookingResponse> bookingResponses = bookingPage.getContent().stream()
                .map(this::convertToBookingResponse)
                .toList();

        return PageResponse.<BookingResponse>builder()
                .content(bookingResponses)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .first(bookingPage.isFirst())
                .last(bookingPage.isLast())
                .build();
    }

    @Override
    public BookingResponse getBookingById(String bookingId) {
        log.info("Getting booking by ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Check if user owns this booking
        Long currentUserId = getCurrentUserId();
        if (!booking.getUserId().equals(currentUserId)) {
            throw new BusinessException("Access denied: You don't have permission to view this booking");
        }

        return convertToBookingResponse(booking);
    }

    @Override
    public BookingResponse getBookingByReference(String bookingReference) {
        log.info("Getting booking by reference: {}", bookingReference);

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingReference));

        // Check if user owns this booking
        Long currentUserId = getCurrentUserId();
        if (!booking.getUserId().equals(currentUserId)) {
            throw new BusinessException("Access denied: You don't have permission to view this booking");
        }

        return convertToBookingResponse(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(String bookingId, String reason) {
        log.info("Cancelling booking: {} with reason: {}", bookingId, reason);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        // Check if user owns this booking
        Long currentUserId = getCurrentUserId();
        if (!booking.getUserId().equals(currentUserId)) {
            throw new BusinessException("Access denied: You don't have permission to cancel this booking");
        }

        // Check if booking can be cancelled
        if (booking.getBookingStatus() == BookingStatus.CANCELLED || booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException("Booking is already cancelled or expired");
        }

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED && booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
            // This is a paid booking, requires refund
            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason(reason);
            booking.setCancelledAt(LocalDateTime.now());
        } else {
            // Unpaid booking, can be cancelled directly
            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason(reason);
            booking.setCancelledAt(LocalDateTime.now());
        }

        booking = bookingRepository.save(booking);

        // Release seat locks
        seatLockService.releaseUserLocks(booking.getFlightId(), booking.getUserId());

        // Update flight available seats
        flightService.updateAvailableSeats(booking.getFlightId(), booking.getSelectedSeats().size(), true);

        // Determine if refund is required
        boolean refundRequired = booking.getPaymentStatus() == PaymentStatus.COMPLETED;

        // Publish booking cancelled event
        BookingCancelledEvent event = BookingCancelledEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .flightId(booking.getFlightId())
                .seatNumbers(booking.getSelectedSeats())
                .cancellationReason(reason)
                .cancelledAt(LocalDateTime.now())
                .refundRequired(refundRequired)
                .transactionId(null)
                .build();

        eventPublisher.publishBookingCancelled(event);

        // Send cancellation email
        try {
            var flightResponse = flightService.getFlightById(booking.getFlightId());
            Flight flight = convertToFlightEntity(flightResponse);
            emailNotificationService.sendBookingCancellationEmail(booking, flight, reason);
        } catch (Exception e) {
            log.error("Failed to send booking cancellation email", e);
        }

        log.info("Booking cancelled successfully: {}", bookingId);
    }

    @Override
    @Transactional
    public void confirmBooking(String bookingId) {
        log.info("Confirming booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED || booking.getBookingStatus() == BookingStatus.CANCELLED || booking.getBookingStatus() == BookingStatus.EXPIRED) {
            log.warn("Booking {} cannot be confirmed from current status: {}", bookingId, booking.getBookingStatus());
            return;
        }

        // Confirm booking
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setConfirmedAt(LocalDateTime.now());

        booking = bookingRepository.save(booking);

        // Confirm seat locks (convert to permanent bookings)
        seatLockService.confirmSeatLocks(
                booking.getFlightId(),
                booking.getSelectedSeats(),
                booking.getUserId(),
                booking.getId()
        );

        // Publish booking confirmed event
        BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .flightId(booking.getFlightId())
                .seatNumbers(booking.getSelectedSeats())
                .transactionId(null)
                .paidAmount(booking.getTotalAmount())
                .confirmedAt(LocalDateTime.now())
                .build();

        eventPublisher.publishBookingConfirmed(event);

        log.info("Booking confirmed successfully: {}", bookingId);
    }

    @Override
    @Transactional
    public void expireBooking(String bookingId) {
        log.info("Expiring booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (booking.getBookingStatus() != BookingStatus.LOCKED) {
            log.warn("Booking {} is not in PENDING_PAYMENT status, cannot expire", bookingId);
            return;
        }

        // Expire booking
        booking.setBookingStatus(BookingStatus.EXPIRED);

        booking = bookingRepository.save(booking);

        // Release seat locks
        seatLockService.releaseUserLocks(booking.getFlightId(), booking.getUserId());

        // Update flight available seats
        flightService.updateAvailableSeats(booking.getFlightId(), booking.getSelectedSeats().size(), true);

        // Publish booking expired event
        BookingExpiredEvent event = BookingExpiredEvent.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .flightId(booking.getFlightId())
                .seatNumbers(booking.getSelectedSeats())
                .expiredAt(LocalDateTime.now())
                .reason("Payment timeout")
                .build();

        eventPublisher.publishBookingExpired(event);

        log.info("Booking expired successfully: {}", bookingId);
    }

    @Override
    @Transactional
    public void updateBookingPaymentStatus(String bookingId, PaymentStatus paymentStatus) {
        log.info("Updating payment status for booking: {} to {}", bookingId, paymentStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        booking.setPaymentStatus(paymentStatus);

        if (paymentStatus == PaymentStatus.COMPLETED) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setConfirmedAt(LocalDateTime.now());
        } else if (paymentStatus == PaymentStatus.FAILED) {
            // Keep bookingStatus as LOCKED to allow retry; scheduled job will expire if needed
        }

        bookingRepository.save(booking);
        log.info("Payment status updated for booking: {}", bookingId);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            // Extract user ID from JWT claims if needed
            return 1L; // TODO: Extract from JWT token
        }
        return 1L; // Default for testing
    }

    private String generateBookingId() {
        return "BK-" + System.currentTimeMillis();
    }

    private String generateBookingReference() {
        return "VN" + System.currentTimeMillis();
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .flightId(booking.getFlightId())
                .flightInfo(null)
                .passengers(booking.getPassengers())
                .selectedSeats(booking.getSelectedSeats())
                .seatClass(booking.getSeatClass())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(booking.getPaymentStatus())
                .contactInfo(booking.getContactInfo())
                .specialRequests(booking.getSpecialRequests())
                .lockExpiresAt(booking.getLockExpiresAt())
                .confirmedAt(booking.getConfirmedAt())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private Flight convertToFlightEntity(com.booking.booking_service.dto.respone.FlightResponse flightResponse) {
        // Convert FlightResponse to Flight entity for email service
        return Flight.builder()
                .id(flightResponse.getId())
                .flightNumber(flightResponse.getFlightNumber())
                .airlineCode(flightResponse.getAirlineCode())
                .airlineName(flightResponse.getAirlineName())
                .departureAirport(flightResponse.getDepartureAirport())
                .arrivalAirport(flightResponse.getArrivalAirport())
                .departureTime(flightResponse.getDepartureTime())
                .arrivalTime(flightResponse.getArrivalTime())
                .build();
    }

    private BigDecimal calculateTotalAmount(com.booking.booking_service.dto.respone.FlightResponse flight,
                                            String seatClass,
                                            int numberOfSeats) {
        BigDecimal pricePerSeat;
        if (seatClass == null) {
            pricePerSeat = flight.getBasePrice();
        } else if ("BUSINESS".equalsIgnoreCase(seatClass)) {
            pricePerSeat = flight.getBusinessPrice() != null ? flight.getBusinessPrice() : flight.getBasePrice();
        } else if ("FIRST".equalsIgnoreCase(seatClass)) {
            pricePerSeat = flight.getFirstPrice() != null ? flight.getFirstPrice() : flight.getBasePrice();
        } else {
            pricePerSeat = flight.getBasePrice();
        }
        return pricePerSeat.multiply(BigDecimal.valueOf(numberOfSeats));
    }

    private List<PassengerInfo> mapPassengers(List<PassengerRequest> passengerRequests) {
        return passengerRequests == null ? List.of() : passengerRequests.stream()
                .map(pr -> PassengerInfo.builder()
                        .title(pr.getTitle())
                        .firstName(pr.getFirstName())
                        .lastName(pr.getLastName())
                        .dateOfBirth(pr.getDateOfBirth())
                        .nationality(pr.getNationality())
                        .passportNumber(pr.getPassportNumber())
                        .passportExpiry(pr.getPassportExpiry())
                        .meal(pr.getMeal())
                        .build())
                .toList();
    }
}