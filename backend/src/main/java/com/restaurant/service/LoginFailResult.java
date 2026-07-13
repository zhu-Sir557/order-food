package com.restaurant.service;

import lombok.Getter;

/**
 * 登录失败结果（内部值对象）
 */
@Getter
public class LoginFailResult {

    /** 是否因失败次数达上限而被锁定（账号或 IP 任一锁定即为 true） */
    private final boolean locked;

    /** 当前失败次数（账号与 IP 取较大值） */
    private final int currentCount;

    /** 失败次数上限 */
    private final int maxCount;

    public LoginFailResult(boolean locked, int currentCount, int maxCount) {
        this.locked = locked;
        this.currentCount = currentCount;
        this.maxCount = maxCount;
    }
}
