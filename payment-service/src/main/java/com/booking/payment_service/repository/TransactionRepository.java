package com.booking.payment_service.repository;

import com.booking.payment_service.entity.Transaction;
import com.booking.payment_service.utils.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByTransactionIdAndDeleted(String transactionId, Boolean deleted);

    List<Transaction> findByBookingIdAndDeleted(Long bookingId, Boolean deleted);

    Page<Transaction> findByUserIdAndDeleted(Long userId, Boolean deleted, Pageable pageable);

    List<Transaction> findByStatusAndDeleted(TransactionStatus status, Boolean deleted);

    @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.nextRetryAt <= :now AND t.retryCount < :maxRetries AND t.deleted = false")
    List<Transaction> findRetryableTransactions(@Param("status") TransactionStatus status,
                                                @Param("now") LocalDateTime now,
                                                @Param("maxRetries") Integer maxRetries);

    @Query("SELECT t FROM Transaction t WHERE t.isPriority = true AND t.status = :status AND t.deleted = false ORDER BY t.createdAt ASC")
    List<Transaction> findPriorityTransactions(@Param("status") TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.status IN :statuses AND t.createdAt BETWEEN :startDate AND :endDate AND t.deleted = false")
    List<Transaction> findByStatusInAndCreatedAtBetween(@Param("statuses") List<TransactionStatus> statuses,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status AND t.createdAt > :since AND t.deleted = false")
    Long countByUserIdAndStatusAndCreatedAtAfter(@Param("userId") Long userId,
                                                 @Param("status") TransactionStatus status,
                                                 @Param("since") LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE t.gatewayTransactionId = :gatewayTransactionId AND t.deleted = false")
    Optional<Transaction> findByGatewayTransactionId(@Param("gatewayTransactionId") String gatewayTransactionId);

    @Modifying
    @Query("UPDATE Transaction t SET t.status = :status, t.processedAt = :processedAt, t.updatedAt = :now WHERE t.id = :id")
    void updateTransactionStatus(@Param("id") Long id,
                                 @Param("status") TransactionStatus status,
                                 @Param("processedAt") LocalDateTime processedAt,
                                 @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Transaction t SET t.retryCount = t.retryCount + 1, t.nextRetryAt = :nextRetryAt, t.updatedAt = :now WHERE t.id = :id")
    void incrementRetryCount(@Param("id") Long id,
                             @Param("nextRetryAt") LocalDateTime nextRetryAt,
                             @Param("now") LocalDateTime now);
}
