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
    ACCOUNT_DISABLED(HttpStatus.LOCKED, "Tài khoản của bạn đã bị khóa."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng"),
    
    // OTP errors
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Mã OTP không đúng"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn"),
    OTP_BLOCKED(HttpStatus.TOO_MANY_REQUESTS, "Bạn đã nhập sai quá nhiều lần, vui lòng thử lại sau"),
    
    // Token errors
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"),

    // Cart errors
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Sản phẩm không có trong giỏ hàng"),
    CART_MAX_ITEMS_EXCEEDED(HttpStatus.BAD_REQUEST, "Giỏ hàng đã đạt tối đa 50 sản phẩm"),
    CART_PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "Sản phẩm không còn khả dụng"),

    // Seller errors
    SELLER_REGISTRATION_EXISTS(HttpStatus.CONFLICT, "Bạn đã đăng ký seller rồi"),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin seller"),
    SELLER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "Tài khoản seller chưa sẵn sàng bán hàng"),
    SELLER_SUSPENDED(HttpStatus.FORBIDDEN, "Tài khoản seller đã bị tạm khóa"),
    SELLER_ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "Seller đã được duyệt trước đó"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Chuyển trạng thái không hợp lệ"),

    // Admin errors
    ADMIN_FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền admin");
    
    private final HttpStatus httpStatus;
    private final String message;
}