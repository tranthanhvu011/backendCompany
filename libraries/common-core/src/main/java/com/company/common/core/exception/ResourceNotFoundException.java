package com.company.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                ErrorCode.valueOf("RESOURCE_NOT_FOUND"),
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.valueOf("RESOURCE_NOT_FOUND"), message, HttpStatus.NOT_FOUND);
    }
}
