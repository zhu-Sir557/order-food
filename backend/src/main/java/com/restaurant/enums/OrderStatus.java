package com.restaurant.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;

/**
 * Order status enum.
 *
 * <p>Represents the lifecycle states of an order:
 * <ul>
 *   <li>PENDING_PAY → PENDING_ACCEPT → COOKING → COMPLETED → PICKED_UP</li>
 *   <li>Any of PENDING_PAY, PENDING_ACCEPT, COOKING can transition to CANCELLED</li>
 * </ul></p>
 */
@Getter
public enum OrderStatus {

    /** Pending payment */
    PENDING_PAY(0, "待支付"),

    /** Pending acceptance (paid, waiting for kitchen to accept) */
    PENDING_ACCEPT(1, "待接单"),

    /** Cooking in progress */
    COOKING(2, "制作中"),

    /** Completed (ready for pickup) */
    COMPLETED(3, "已完成"),

    /** Picked up by customer */
    PICKED_UP(4, "已取餐"),

    /** Cancelled */
    CANCELLED(5, "已取消");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Get OrderStatus by code.
     *
     * @param code the status code
     * @return the matching OrderStatus, or null if not found
     */
    public static OrderStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if this status can transit to the target status.
     *
     * <p>Valid transitions:
     * <ul>
     *   <li>PENDING_PAY → PENDING_ACCEPT, CANCELLED</li>
     *   <li>PENDING_ACCEPT → COOKING, CANCELLED</li>
     *   <li>COOKING → COMPLETED, CANCELLED</li>
     *   <li>COMPLETED → PICKED_UP</li>
     *   <li>PICKED_UP → (terminal, no transitions)</li>
     *   <li>CANCELLED → (terminal, no transitions)</li>
     * </ul></p>
     *
     * @param target the target status to transit to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitTo(OrderStatus target) {
        if (target == null) {
            return false;
        }
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(this, EnumSet.noneOf(OrderStatus.class));
        return allowed.contains(target);
    }

    /** Pre-computed valid transition map */
    private static final java.util.Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS;

    static {
        java.util.Map<OrderStatus, Set<OrderStatus>> map = new java.util.EnumMap<>(OrderStatus.class);
        map.put(PENDING_PAY, EnumSet.of(PENDING_ACCEPT, CANCELLED));
        map.put(PENDING_ACCEPT, EnumSet.of(COOKING, CANCELLED));
        map.put(COOKING, EnumSet.of(COMPLETED, CANCELLED));
        map.put(COMPLETED, EnumSet.of(PICKED_UP));
        map.put(PICKED_UP, EnumSet.noneOf(OrderStatus.class));
        map.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        VALID_TRANSITIONS = java.util.Collections.unmodifiableMap(map);
    }
}
