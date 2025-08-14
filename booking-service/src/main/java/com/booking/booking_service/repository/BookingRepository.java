package com.booking.booking_service.repository;

import com.booking.booking_service.entity.Booking;
import com.booking.booking_service.utils.BookingStatus;
import com.booking.booking_service.utils.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    List<Booking> findByFlightId(String flightId);

    @Query("{ 'userId': ?0, 'bookingStatus': ?1 }")
    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    @Query("{ 'bookingStatus': ?0 }")
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    @Query("{ 'paymentStatus': ?0 }")
    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("{ 'lockExpiresAt': { $lt: ?0 }, 'bookingStatus': 'LOCKED' }")
    List<Booking> findExpiredLocks(LocalDateTime currentTime);

    @Query("{ 'flightId': ?0, 'selectedSeats': { $in: ?1 }, 'bookingStatus': { $in: ['LOCKED', 'CONFIRMED'] } }")
    List<Booking> findConflictingBookings(String flightId, List<String> seatNumbers);

    @Query("{ 'createdAt': { $gte: ?0, $lt: ?1 } }")
    List<Booking> findBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ 'userId': ?0, 'createdAt': { $gte: ?1 } }")
    Page<Booking> findRecentBookingsByUser(Long userId, LocalDateTime since, Pageable pageable);
}