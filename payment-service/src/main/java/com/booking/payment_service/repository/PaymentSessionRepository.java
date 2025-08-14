package com.booking.payment_service.repository;



import com.booking.payment_service.entity.PaymentSession;
import com.booking.payment_service.utils.PaymentSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentSessionRepository extends JpaRepository<PaymentSession, Long> {

    Optional<PaymentSession> findBySessionIdAndDeleted(String sessionId, Boolean deleted);

    Optional<PaymentSession> findByBookingIdAndStatusAndDeleted(Long bookingId, PaymentSessionStatus status, Boolean deleted);

    List<PaymentSession> findByUserIdAndDeleted(Long userId, Boolean deleted);

    @Query("SELECT ps FROM PaymentSession ps WHERE ps.status = 'PENDING' AND ps.expiresAt < :now AND ps.deleted = false")
    List<PaymentSession> findExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PaymentSession ps SET ps.status = 'EXPIRED', ps.updatedAt = :now WHERE ps.status = 'PENDING' AND ps.expiresAt < :now")
    int expireOldSessions(@Param("now") LocalDateTime now);

    Optional<PaymentSession> findByGatewaySessionIdAndDeleted(String gatewaySessionId, Boolean deleted);
}
