package com.company.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.valueOf("VALIDATION_ERROR"), message, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String field, String message) {
        super(ErrorCode.valueOf("VALIDATION_ERROR"),
            String.format("Validation failed for field '%s': %s", field, message), 
            HttpStatus.BAD_REQUEST);
    }
}
