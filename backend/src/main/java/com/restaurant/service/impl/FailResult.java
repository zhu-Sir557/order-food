package com.restaurant.service.impl;

import lombok.Getter;

/**
 * 验证码校验失败结果（内部值对象）
 */
@Getter
public class FailResult {

    /** 是否因错误次数达上限而被锁定 */
    private final boolean locked;

    /** 当前错误次数（自增后） */
    private final int currentCount;

    /** 错误次数上限 */
    private final int maxCount;

    public FailResult(boolean locked, int currentCount, int maxCount) {
        this.locked = locked;
        this.currentCount = currentCount;
        this.maxCount = maxCount;
    }
}
