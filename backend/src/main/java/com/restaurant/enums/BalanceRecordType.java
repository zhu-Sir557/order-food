package com.restaurant.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 余额变动记录类型枚举
 */
@Getter
public enum BalanceRecordType {

    /** 充值 */
    RECHARGE(1, "充值"),

    /** 消费 */
    CONSUME(2, "消费");

    private final int code;
    private final String desc;

    BalanceRecordType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举值
     *
     * @param code 类型码
     * @return 对应的枚举值，未找到返回 null
     */
    public static BalanceRecordType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElse(null);
    }
}
