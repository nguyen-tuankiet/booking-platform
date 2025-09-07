package com.booking.notification_service.repository;

import com.booking.notification_service.entity.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByNameAndTypeAndActiveTrue(String name, String type);

    List<NotificationTemplate> findByTypeAndActiveTrue(String type);

    List<NotificationTemplate> findByActiveTrue();

    Optional<NotificationTemplate> findByNameAndTypeAndLanguageAndActiveTrue(
            String name, String type, String language);
}