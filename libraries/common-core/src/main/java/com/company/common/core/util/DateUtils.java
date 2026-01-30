package com.company.common.core.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 */
public final class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);

    /**
     * Get current date time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Get current date time in UTC
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Format LocalDateTime to string
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    /**
     * Format LocalDateTime with custom pattern
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern(pattern)) : null;
    }

    /**
     * Parse string to LocalDateTime
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER) : null;
    }

    /**
     * Parse string to LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    /**
     * Convert LocalDateTime to epoch milliseconds
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Convert epoch milliseconds to LocalDateTime
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    /**
     * Get start of day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Get end of day
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Check if date is between two dates
     */
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date != null && start != null && end != null 
                && !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Calculate days between two dates
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Add days to date
     */
    public static LocalDateTime addDays(LocalDateTime date, int days) {
        return date.plusDays(days);
    }

    /**
     * Check if datetime is expired
     */
    public static boolean isExpired(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }
}
