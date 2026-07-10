package com.restaurant.common;

/**
 * Standard result codes for API responses.
 */
public enum ResultCode {

    /** Success */
    SUCCESS(200, "操作成功"),

    /** Bad request / parameter error */
    PARAM_ERROR(400, "参数错误"),

    /** Unauthorized */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** Forbidden */
    FORBIDDEN(403, "无权限访问"),

    /** Server error */
    SERVER_ERROR(500, "服务器内部错误"),

    /** 手机号格式非法 */
    SMS_PHONE_INVALID(400, "请输入正确的手机号"),

    /** 滑块验证码无效或未通过（Q2 前置） */
    CAPTCHA_INVALID(400, "验证码无效或未通过"),

    /** 验证码不存在或已过期 */
    SMS_CODE_EXPIRED(400, "验证码已过期，请重新获取"),

    /** 验证码错误（未达上限） */
    SMS_CODE_ERROR(400, "验证码错误"),

    /** 阿里云调用失败 / 限流（不暴露原始错误） */
    SMS_SEND_FAILED(429, "验证码发送失败，请稍后再试"),

    /** 发送间隔不足 */
    RATE_LIMIT_PHONE_INTERVAL(429, "发送过于频繁，请稍后再试"),

    /** 单号日上限 */
    RATE_LIMIT_PHONE_DAILY(429, "今日验证码发送次数已达上限，请明天再试"),

    /** 单 IP 日上限 */
    RATE_LIMIT_IP_DAILY(429, "当前网络发送过于频繁，请稍后再试"),

    /** 验证码错误次数过多，被锁定 */
    RATE_LIMIT_LOCKED(429, "验证码错误次数过多，请 10 分钟后再试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
