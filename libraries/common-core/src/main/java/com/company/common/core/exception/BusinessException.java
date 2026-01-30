package com.company.common.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all business exceptions.
 * All custom exceptions should extend this class.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Object[] args;

    // Constructor với ErrorCode enum (khuyên dùng)
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.args = args;
    }

    // Constructor legacy (cho backward compatibility)
    public BusinessException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.args = null;
    }

    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.httpStatus = httpStatus;
        this.args = null;
    }
}
