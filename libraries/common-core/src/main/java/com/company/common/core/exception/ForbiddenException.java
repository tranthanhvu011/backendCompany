package com.company.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user does not have permission to access a resource.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException() {
        super(ErrorCode.valueOf("FORBIDDEN"), "You do not have permission to access this resource", HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ErrorCode.valueOf("FORBIDDEN"), message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException(String resource, String action) {
        super(ErrorCode.valueOf("FORBIDDEN"),
            String.format("You do not have permission to %s %s", action, resource), 
            HttpStatus.FORBIDDEN);
    }
}
