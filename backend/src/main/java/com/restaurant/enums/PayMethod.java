package com.restaurant.enums;

import java.util.Arrays;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
public enum PayMethod {

    /** 微信支付 */
    WECHAT(1, "微信支付"),

    /** 支付宝 */
    ALIPAY(2, "支付宝"),

    /** 余额支付 */
    BALANCE(3, "余额支付");

    private final int code;
    private final String desc;

    PayMethod(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举值
     *
     * @param code 支付方式码
     * @return 对应的枚举值，未找到返回 null
     */
    public static PayMethod fromCode(int code) {
        return Arrays.stream(values())
                .filter(method -> method.code == code)
                .findFirst()
                .orElse(null);
    }
}
