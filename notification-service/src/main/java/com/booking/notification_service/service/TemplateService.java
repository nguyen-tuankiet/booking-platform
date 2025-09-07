package com.booking.notification_service.service;

import java.util.Map;

public interface TemplateService {
    String processTemplate(String templateName, String type, Map<String, Object> data);
    String processSubject(String templateName, String type, Map<String, Object> data);
}
