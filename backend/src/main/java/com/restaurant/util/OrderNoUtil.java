package com.restaurant.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility for generating unique order numbers.
 *
 * <p>Format: yyyyMMddHHmmss + 4-digit random number (18 characters total).</p>
 */
public final class OrderNoUtil {

    /** Date-time format pattern for order number prefix */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** Random number upper bound (exclusive) */
    private static final int RANDOM_BOUND = 10000;

    /**
     * Private constructor to prevent instantiation.
     */
    private OrderNoUtil() {
    }

    /**
     * Generate a unique order number.
     *
     * <p>Format: yyyyMMddHHmmss + 4-digit zero-padded random number.</p>
     *
     * @return the generated order number string
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(RANDOM_BOUND);
        return timestamp + String.format("%04d", random);
    }
}
