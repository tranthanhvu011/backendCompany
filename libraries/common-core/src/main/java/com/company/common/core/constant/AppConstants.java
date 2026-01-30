package com.company.common.core.constant;

/**
 * Application-wide constants.
 */
public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Constants class");
    }

    // ========== Pagination ==========
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // ========== Date/Time Formats ==========
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    // ========== Headers ==========
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_BEARER_PREFIX = "Bearer ";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_TENANT_ID = "X-Tenant-ID";

    // ========== Error Messages ==========
    public static final String GENERIC_ERROR_MESSAGE = "An unexpected error occurred. Please try again later.";
    public static final String VALIDATION_ERROR_MESSAGE = "Validation failed. Please check your input.";
    public static final String UNAUTHORIZED_MESSAGE = "You are not authorized to access this resource.";
    public static final String FORBIDDEN_MESSAGE = "You do not have permission to perform this action.";

    // ========== Regex Patterns ==========
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final String PHONE_REGEX = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";

    // ========== Cache Keys Prefix ==========
    public static final String CACHE_PREFIX_USER = "user::";
    public static final String CACHE_PREFIX_TOKEN = "token::";
    public static final String CACHE_PREFIX_SESSION = "session::";

    // ========== Kafka Topics ==========
    public static final String TOPIC_USER_CREATED = "user.created";
    public static final String TOPIC_USER_UPDATED = "user.updated";
    public static final String TOPIC_ORDER_CREATED = "order.created";
    public static final String TOPIC_ORDER_COMPLETED = "order.completed";
    public static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
    public static final String TOPIC_NOTIFICATION = "notification.send";
}
