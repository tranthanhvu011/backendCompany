package com.company.common.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // General errors
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    OTP_MAX_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "Bạn đã nhập sai 3 lần, vui lòng gửi lại mã OTP"),
    // Auth errors
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã được sử dụng"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "Username đã được sử dụng"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy user"),
    
    // OTP errors
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Mã OTP không đúng"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn"),
    OTP_BLOCKED(HttpStatus.TOO_MANY_REQUESTS, "Bạn đã nhập sai quá nhiều lần, vui lòng thử lại sau"),
    
    // Token errors
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ");
    
    private final HttpStatus httpStatus;
    private final String message;
}