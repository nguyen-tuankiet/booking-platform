package com.booking.booking_service.service;

import com.booking.booking_service.entity.SeatLock;

import java.util.List;

public interface SeatLockService {
    SeatLock lockSeat(String flightId, String seatNumber, Long userId, String sessionId);

    List<SeatLock> lockSeats(String flightId, List<String> seatNumbers, Long userId, String sessionId);

    boolean isSeatLocked(String flightId, String seatNumber);

    void releaseSeatLock(String flightId, String seatNumber, Long userId);

    void releaseUserLocks(String flightId, Long userId);

    void releaseSessionLocks(String sessionId);

    void confirmSeatLocks(String flightId, List<String> seatNumbers, Long userId, String bookingId);

    List<SeatLock> getUserActiveLocks(Long userId);

    void extendSeatLock(String flightId, String seatNumber, Long userId, int additionalMinutes);

    void cleanupExpiredLocks();

    boolean hasUserLockedSeats(String flightId, Long userId);

    List<String> getUserLockedSeats(String flightId, Long userId);
}
