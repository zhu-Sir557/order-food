package com.restaurant.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 点卡状态枚举
 */
@Getter
public enum CardStatus {

    /** 未使用 */
    UNUSED(0, "未使用"),

    /** 已发放 */
    ASSIGNED(1, "已发放"),

    /** 已使用 */
    USED(2, "已使用");

    private final int code;
    private final String desc;

    CardStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举值
     *
     * @param code 状态码
     * @return 对应的枚举值，未找到返回 null
     */
    public static CardStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }
}
