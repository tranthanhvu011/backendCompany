package com.company.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user is not authorized to perform an action.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ErrorCode.valueOf("UNAUTHORIZED"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.valueOf("UNAUTHORIZED"), message, HttpStatus.UNAUTHORIZED);
    }
}
