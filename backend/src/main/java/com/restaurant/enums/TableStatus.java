package com.restaurant.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * Dining table status enum.
 */
@Getter
public enum TableStatus {

    /** Idle (available) */
    IDLE(0, "空闲"),

    /** In use (customers seated) */
    IN_USE(1, "就餐中");

    private final int code;
    private final String desc;

    TableStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Get TableStatus by code.
     *
     * @param code the status code
     * @return the matching TableStatus, or null if not found
     */
    public static TableStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }
}
