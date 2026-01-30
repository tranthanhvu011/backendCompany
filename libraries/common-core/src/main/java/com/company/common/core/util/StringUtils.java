package com.company.common.core.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utility class for string operations.
 */
public final class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMERIC = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Generate random UUID string (without dashes)
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate random alphanumeric string
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Generate random numeric string (for OTP)
     */
    public static String generateOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NUMERIC.charAt(RANDOM.nextInt(NUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Mask email address (show first 2 chars and domain)
     * Example: john.doe@email.com -> jo****@email.com
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        
        if (name.length() <= 2) {
            return name + "****@" + domain;
        }
        return name.substring(0, 2) + "****@" + domain;
    }

    /**
     * Mask phone number (show last 4 digits)
     * Example: 0987654321 -> ******4321
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 4) {
            return phone;
        }
        int visibleDigits = 4;
        int maskedLength = phone.length() - visibleDigits;
        return "*".repeat(maskedLength) + phone.substring(maskedLength);
    }

    /**
     * Convert to slug format
     * Example: "Hello World" -> "hello-world"
     */
    public static String toSlug(String input) {
        if (isEmpty(input)) {
            return input;
        }
        return input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Truncate string with ellipsis
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Capitalize first letter
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
