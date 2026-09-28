package com.rikkeibank.common.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id), 404, "Not Found");
    }

    public ResourceNotFoundException(String message) {
        super(message, 404, "Not Found");
    }
}
