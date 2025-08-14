package com.booking.booking_service.repository;

import com.booking.booking_service.entity.SeatLock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatLockRepository extends MongoRepository<SeatLock, String> {

    @Query("{ 'flightId': ?0, 'seatNumber': ?1, 'status': 'ACTIVE' }")
    Optional<SeatLock> findActiveLockBySeat(String flightId, String seatNumber);

    @Query("{ 'flightId': ?0, 'seatNumber': { $in: ?1 }, 'status': 'ACTIVE' }")
    List<SeatLock> findActiveLocksBySeats(String flightId, List<String> seatNumbers);

    @Query("{ 'userId': ?0, 'status': 'ACTIVE' }")
    List<SeatLock> findActiveLocksByUser(Long userId);

    @Query("{ 'sessionId': ?0, 'status': 'ACTIVE' }")
    List<SeatLock> findActiveLocksBySession(String sessionId);

    @Query("{ 'expiresAt': { $lt: ?0 }, 'status': 'ACTIVE' }")
    List<SeatLock> findExpiredLocks(LocalDateTime currentTime);

    @Query("{ 'flightId': ?0, 'userId': ?1, 'status': 'ACTIVE' }")
    List<SeatLock> findActiveLocksByFlightAndUser(String flightId, Long userId);

    @Query("{ 'bookingId': ?0 }")
    List<SeatLock> findByBookingId(String bookingId);

    void deleteByFlightIdAndSeatNumberAndUserId(String flightId, String seatNumber, Long userId);

    void deleteBySessionId(String sessionId);
}