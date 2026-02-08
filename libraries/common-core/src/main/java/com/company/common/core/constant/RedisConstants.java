package com.company.common.core.constant;

    public final class RedisConstants {
        public static final String OTP_PREFIX = "otp:";
        public static final String REGISTER_PREFIX = "register:";
        public static final String RESETPASSWORD_PREFIX = "resetPassword:";
        public static final String RESETPASSWORD_EMAIL_PREFIX = "reset-email:";

        public static final String REFRESH_TOKEN_PREFIX = "refresh:";
        public static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
        public static final String OTP_BLOCK_PREFIX = "otp_block:";
public static final String TOTAL_ATTEMPTS_PREFIX = "total_attempts:";
public static final int MAX_TOTAL_ATTEMPTS = 9; // 3 OTP x 3 lần = 9
public static final long BLOCK_EXPIRE_MINUTES = 15;
        public static final long OTP_EXPIRE_MINUTES = 5;
        public static final long REGISTER_EXPIRE_MINUTES = 10;
        public static final long REFRESH_EXPIRE_DAYS = 7;
        public static final int MAX_OTP_ATTEMPTS = 3;
        public static final long RESET_PASSWORD_EXPIRE_MINUTES = 15; // 15 phút

        private RedisConstants() {} // Không cho tạo instance
    }

