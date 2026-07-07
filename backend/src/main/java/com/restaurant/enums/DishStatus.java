package com.restaurant.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * Dish status enum.
 */
@Getter
public enum DishStatus {

    /** Off shelf (not available for ordering) */
    OFF_SHELF(0, "下架"),

    /** On shelf (available for ordering) */
    ON_SHELF(1, "上架");

    private final int code;
    private final String desc;

    DishStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Get DishStatus by code.
     *
     * @param code the status code
     * @return the matching DishStatus, or null if not found
     */
    public static DishStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }
}
