package com.restaurant.service.impl;

import lombok.Getter;

/**
 * 发送限频判定结果（内部值对象）
 *
 * <p>由 {@link SmsRateLimiter#canSend(String, String)} 返回，供业务层映射为对应的
 * {@code ResultCode} 与友好文案。</p>
 */
@Getter
public class RateLimitResult {

    /** 是否允许发送 */
    private final boolean allowed;

    /** 是否被锁定（错误次数达上限） */
    private final boolean locked;

    /** 剩余秒数（主要用于发送间隔场景） */
    private final long remainSeconds;

    /** 剩余可发送次数（主要用于日上限场景，已为非负值） */
    private final int remainCount;

    /** 触发原因 */
    private final Reason reason;

    private RateLimitResult(boolean allowed, boolean locked, long remainSeconds,
                            int remainCount, Reason reason) {
        this.allowed = allowed;
        this.locked = locked;
        this.remainSeconds = remainSeconds;
        this.remainCount = remainCount;
        this.reason = reason;
    }

    public static RateLimitResult ok() {
        return new RateLimitResult(true, false, 0, 0, Reason.OK);
    }

    public static RateLimitResult locked() {
        return new RateLimitResult(false, true, 0, 0, Reason.LOCKED);
    }

    public static RateLimitResult interval(long remainSeconds, int remainCount) {
        return new RateLimitResult(false, false, remainSeconds, remainCount, Reason.INTERVAL);
    }

    public static RateLimitResult phoneDaily(int remainCount) {
        return new RateLimitResult(false, false, 0, remainCount, Reason.PHONE_DAILY);
    }

    public static RateLimitResult ipDaily(int remainCount) {
        return new RateLimitResult(false, false, 0, remainCount, Reason.IP_DAILY);
    }

    /** 限频触发原因 */
    public enum Reason {
        /** 通过 */
        OK,
        /** 账号被锁定 */
        LOCKED,
        /** 发送间隔不足 */
        INTERVAL,
        /** 单号日上限 */
        PHONE_DAILY,
        /** 单 IP 日上限 */
        IP_DAILY
    }
}
