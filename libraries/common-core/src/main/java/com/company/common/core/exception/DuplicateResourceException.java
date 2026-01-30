package com.company.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource already exists (duplicate).
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
            "DUPLICATE_RESOURCE",
            String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.CONFLICT
        );
    }

    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message, HttpStatus.CONFLICT);
    }
}
