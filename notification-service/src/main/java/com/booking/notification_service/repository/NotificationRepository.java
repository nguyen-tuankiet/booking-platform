package com.booking.notification_service.repository;

import com.booking.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification,String> {
    List<Notification> findByStatusAndScheduledAtBefore(String status, LocalDateTime dateTime);

    List<Notification> findByStatusAndRetryCountLessThan(String status, int maxRetries);

    Page<Notification> findByUserId(String userId, Pageable pageable);

    Page<Notification> findByUserIdAndType(String userId, String type, Pageable pageable);

    @Query("{ 'status' : ?0, 'createdAt' : { $gte: ?1, $lte: ?2 } }")
    List<Notification> findByStatusAndDateRange(String status, LocalDateTime from, LocalDateTime to);

    long countByStatusAndCreatedAtBetween(String status, LocalDateTime from, LocalDateTime to);

    @Query("{ 'priority' : { $gte: ?0 }, 'status' : ?1 }")
    List<Notification> findHighPriorityPendingNotifications(int minPriority, String status);
}
