package com.booking.payment_service.repository;



import com.booking.payment_service.entity.Refund;
import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.utils.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundIdAndDeleted(String refundId, Boolean deleted);

    List<Refund> findByTransactionAndDeleted(Transaction transaction, Boolean deleted);

    Page<Refund> findByTransaction_UserIdAndDeleted(Long userId, Boolean deleted, Pageable pageable);

    List<Refund> findByStatusAndDeleted(RefundStatus status, Boolean deleted);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.transaction = :transaction AND r.status = 'SUCCESS' AND r.deleted = false")
    BigDecimal getTotalRefundedAmount(@Param("transaction") Transaction transaction);

    @Query("SELECT r FROM Refund r WHERE r.status IN :statuses AND r.createdAt BETWEEN :startDate AND :endDate AND r.deleted = false")
    List<Refund> findByStatusInAndCreatedAtBetween(@Param("statuses") List<RefundStatus> statuses,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    Optional<Refund> findByGatewayRefundIdAndDeleted(String gatewayRefundId, Boolean deleted);
}