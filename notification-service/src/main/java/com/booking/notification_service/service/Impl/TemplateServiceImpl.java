package com.booking.notification_service.service.Impl;

import com.booking.notification_service.entity.NotificationTemplate;
import com.booking.notification_service.repository.NotificationTemplateRepository;
import com.booking.notification_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public String processTemplate(String templateName, String type, Map<String, Object> data) {
        Optional<NotificationTemplate> templateOpt =
                templateRepository.findByNameAndTypeAndActiveTrue(templateName, type);

        if (templateOpt.isEmpty()) {
            log.warn("Template not found: {} for type: {}", templateName, type);
            return getDefaultTemplate(type, data);
        }

        NotificationTemplate template = templateOpt.get();
        return replacePlaceholders(template.getContent(), data);
    }

    @Override
    public String processSubject(String templateName, String type, Map<String, Object> data) {
        Optional<NotificationTemplate> templateOpt =
                templateRepository.findByNameAndTypeAndActiveTrue(templateName, type);

        if (templateOpt.isEmpty()) {
            return "Notification from Booking System";
        }

        NotificationTemplate template = templateOpt.get();
        return replacePlaceholders(template.getSubject(), data);
    }

    private String replacePlaceholders(String template, Map<String, Object> data) {
        String result = template;

        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }

        return result;
    }

    private String getDefaultTemplate(String type, Map<String, Object> data) {
        switch (type.toUpperCase()) {
            case "EMAIL":
                return "Dear Customer,\n\nThis is a notification from Booking System.\n\nBest regards,\nBooking Team";
            case "SMS":
                return "Booking System: " + data.getOrDefault("message", "You have a new notification");
            default:
                return "You have a new notification from Booking System";
        }
    }
}

