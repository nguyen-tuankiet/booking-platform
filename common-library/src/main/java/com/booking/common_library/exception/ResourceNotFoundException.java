package com.booking.common_library.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value), "RESOURCE_NOT_FOUND");
    }
}