package com.booking.booking_service.service.Impl;

import com.booking.booking_service.entity.SeatLock;
import com.booking.booking_service.repository.SeatLockRepository;
import com.booking.booking_service.service.SeatLockService;
import com.booking.booking_service.utils.LockStatus;
import com.booking.common_library.exception.BusinessException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockServiceImpl implements SeatLockService {

    private final SeatLockRepository seatLockRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.booking.seat-lock-duration:15}")
    private int seatLockDurationMinutes;

    private static final String SEAT_LOCK_PREFIX = "seat_lock:";

    /**
     * Lock ghế cho user trong thời gian nhất định
     */
    @Transactional
    @Override
    public SeatLock lockSeat(String flightId, String seatNumber, Long userId, String sessionId) {
        log.info("Attempting to lock seat {} for flight {} by user {}", seatNumber, flightId, userId);

        // Kiểm tra xem ghế đã bị lock chưa
        if (isSeatLocked(flightId, seatNumber)) {
            throw new BusinessException("Seat " + seatNumber + " is already locked by another user");
        }

        // Tạo lock key cho Redis
        String lockKey = SEAT_LOCK_PREFIX + flightId + ":" + seatNumber;

        // Set distributed lock trong Redis với expiration
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, userId.toString(), seatLockDurationMinutes, TimeUnit.MINUTES);

        if (!lockAcquired) {
            throw new BusinessException("Cannot acquire lock for seat " + seatNumber);
        }

        // Tạo seat lock record trong database
        LocalDateTime now = LocalDateTime.now();
        SeatLock seatLock = SeatLock.builder()
                .flightId(flightId)
                .seatNumber(seatNumber)
                .userId(userId)
                .sessionId(sessionId)
                .lockedAt(now)
                .expiresAt(now.plusMinutes(seatLockDurationMinutes))
                .status(LockStatus.ACTIVE)
                .build();

        seatLock = seatLockRepository.save(seatLock);
        log.info("Successfully locked seat {} for user {} until {}", seatNumber, userId, seatLock.getExpiresAt());

        return seatLock;
    }

    @Override
    @Transactional
    public List<SeatLock> lockSeats(String flightId, List<String> seatNumbers, Long userId, String sessionId) {
        log.info("Attempting to lock {} seats for flight {} by user {}", seatNumbers.size(), flightId, userId);

        // Kiểm tra tất cả ghế trước khi lock
        for (String seatNumber : seatNumbers) {
            if (isSeatLocked(flightId, seatNumber)) {
                throw new BusinessException("Seat " + seatNumber + " is already locked");
            }
        }

        // Lock tất cả ghế
        return seatNumbers.stream()
                .map(seatNumber -> lockSeat(flightId, seatNumber, userId, sessionId))
                .toList();
    }


    /**
     * Kiểm tra ghế có bị lock không
     */
    @Override
    public boolean isSeatLocked(String flightId, String seatNumber) {
        String lockKey = SEAT_LOCK_PREFIX + flightId + ":" + seatNumber;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * Giải phóng lock ghế
     */
    @Override
    @Transactional
    public void releaseSeatLock(String flightId, String seatNumber, Long userId) {
        log.info("Releasing seat lock for seat {} on flight {} by user {}", seatNumber, flightId, userId);

        String lockKey = SEAT_LOCK_PREFIX + flightId + ":" + seatNumber;

        // Kiểm tra user có quyền release lock không
        String lockedByUser = (String) redisTemplate.opsForValue().get(lockKey);
        if (lockedByUser != null && !lockedByUser.equals(userId.toString())) {
            throw new BusinessException("Cannot release seat lock owned by another user");
        }

        // Xóa lock trong Redis
        redisTemplate.delete(lockKey);

        // Update status trong database
        seatLockRepository.findActiveLockBySeat(flightId, seatNumber)
                .ifPresent(lock -> {
                    lock.setStatus(LockStatus.RELEASED);
                    seatLockRepository.save(lock);
                });
    }

    /**
     * Giải phóng tất cả lock của user cho flight
     */
    @Override
    @Transactional
    public void releaseUserLocks(String flightId, Long userId) {
        log.info("Releasing all seat locks for user {} on flight {}", userId, flightId);

        List<SeatLock> userLocks = seatLockRepository.findActiveLocksByFlightAndUser(flightId, userId);

        for (SeatLock lock : userLocks) {
            String lockKey = SEAT_LOCK_PREFIX + flightId + ":" + lock.getSeatNumber();
            redisTemplate.delete(lockKey);

            lock.setStatus(LockStatus.RELEASED);
            seatLockRepository.save(lock);
        }
    }

    /**
     * Giải phóng tất cả lock của session
     */
    @Override
    @Transactional
    public void releaseSessionLocks(String sessionId) {
        log.info("Releasing all seat locks for session {}", sessionId);

        List<SeatLock> sessionLocks = seatLockRepository.findActiveLocksBySession(sessionId);

        for (SeatLock lock : sessionLocks) {
            String lockKey = SEAT_LOCK_PREFIX + lock.getFlightId() + ":" + lock.getSeatNumber();
            redisTemplate.delete(lockKey);

            lock.setStatus(LockStatus.RELEASED);
            seatLockRepository.save(lock);
        }
    }


    /**
     * Confirm lock thành booking
     */
    @Override
    @Transactional
    public void confirmSeatLocks(String flightId, List<String> seatNumbers, Long userId, String bookingId) {
        log.info("Confirming seat locks for booking {} by user {}", bookingId, userId);

        for (String seatNumber : seatNumbers) {
            seatLockRepository.findActiveLockBySeat(flightId, seatNumber)
                    .ifPresent(lock -> {
                        if (!lock.getUserId().equals(userId)) {
                            throw new BusinessException("Cannot confirm seat lock owned by another user");
                        }

                        lock.setStatus(LockStatus.CONFIRMED);
                        lock.setBookingId(bookingId);
                        seatLockRepository.save(lock);
                    });

            // Xóa lock trong Redis vì đã confirm
            String lockKey = SEAT_LOCK_PREFIX + flightId + ":" + seatNumber;
            redisTemplate.delete(lockKey);
        }
    }


    /**
     * Lấy thông tin lock của user
     */
    @Override
    public List<SeatLock> getUserActiveLocks(Long userId) {
        return seatLockRepository.findActiveLocksByUser(userId);
    }

    /**
     * Extend thời gian lock
     */
    @Override
    @Transactional
    public void extendSeatLock(String flightId, String seatNumber, Long userId, int additionalMinutes) {
        log.info("Extending seat lock for seat {} by {} minutes", seatNumber, additionalMinutes);

        String lockKey = SEAT_LOCK_PREFIX + flightId + ":" + seatNumber;

        // Kiểm tra user có quyền extend không
        String lockedByUser = (String) redisTemplate.opsForValue().get(lockKey);
        if (lockedByUser == null || !lockedByUser.equals(userId.toString())) {
            throw new BusinessException("Cannot extend seat lock not owned by user");
        }

        // Extend lock trong Redis
        redisTemplate.expire(lockKey, seatLockDurationMinutes + additionalMinutes, TimeUnit.MINUTES);

        // Update database
        seatLockRepository.findActiveLockBySeat(flightId, seatNumber)
                .ifPresent(lock -> {
                    lock.setExpiresAt(lock.getExpiresAt().plusMinutes(additionalMinutes));
                    seatLockRepository.save(lock);
                });
    }

    /**
     * Cleanup expired locks - chạy định kỳ mỗi phút
     */
    @Override
    @Scheduled(fixedRate = 60000) // 1 phút
    @Async
    public void cleanupExpiredLocks() {
        log.debug("Running cleanup for expired seat locks");

        LocalDateTime now = LocalDateTime.now();
        List<SeatLock> expiredLocks = seatLockRepository.findExpiredLocks(now);

        for (SeatLock lock : expiredLocks) {
            try {
                // Xóa lock trong Redis
                String lockKey = SEAT_LOCK_PREFIX + lock.getFlightId() + ":" + lock.getSeatNumber();
                redisTemplate.delete(lockKey);

                // Update status trong database
                lock.setStatus(LockStatus.EXPIRED);
                seatLockRepository.save(lock);

                log.debug("Cleaned up expired lock for seat {} on flight {}",
                        lock.getSeatNumber(), lock.getFlightId());

            } catch (Exception e) {
                log.error("Error cleaning up expired lock {}: {}", lock.getId(), e.getMessage());
            }
        }

        if (!expiredLocks.isEmpty()) {
            log.info("Cleaned up {} expired seat locks", expiredLocks.size());
        }
    }

    /**
     * Kiểm tra user có lock ghế nào trên flight không
     */
    @Override
    public boolean hasUserLockedSeats(String flightId, Long userId) {
        List<SeatLock> userLocks = seatLockRepository.findActiveLocksByFlightAndUser(flightId, userId);
        return !userLocks.isEmpty();
    }

    /**
     * Lấy danh sách ghế đã lock của user trên flight
     */
    @Override
    public List<String> getUserLockedSeats(String flightId, Long userId) {
        return seatLockRepository.findActiveLocksByFlightAndUser(flightId, userId)
                .stream()
                .map(SeatLock::getSeatNumber)
                .toList();
    }
}
