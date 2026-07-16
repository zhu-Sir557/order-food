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
    RATE_LIMIT_LOCKED(429, "验证码错误次数过多，请 10 分钟后再试"),

    /** 今日验证码发送总量达上限（全站合计） */
    RATE_LIMIT_GLOBAL_DAILY(429, "今日验证码发送总量已达上限，请明天再试"),

    /** 账号/网络被登录失败锁定 */
    LOGIN_LOCKED(429, "账号或网络被锁定，请稍后再试"),

    /** 手机号已被其他账号注册 */
    PHONE_ALREADY_REGISTERED(400, "该手机号已被注册"),

    /** 已绑定手机，不支持改绑 */
    PHONE_ALREADY_BOUND(400, "已绑定手机，不支持改绑"),

    /** 已设置密码 */
    PASSWORD_ALREADY_SET(400, "已设置密码"),

    /** 尚未设置密码（手机号+密码登录时） */
    PASSWORD_NOT_SET(400, "尚未设置密码，请先设置密码"),

    /** 账号未绑定手机号，无法用验证码登录 */
    ACCOUNT_NO_PHONE(400, "账号未绑定手机号，请先绑定手机"),

    /** 账号不存在 */
    ACCOUNT_NOT_FOUND(400, "账号不存在"),

    /** 昵称超长 */
    NICK_TOO_LONG(400, "昵称不能超过20个字符"),

    /** 今日昵称修改次数达上限 */
    NICK_CHANGE_LIMIT(429, "今日昵称修改次数已达上限"),

    /** 今日头像修改次数达上限 */
    AVATAR_CHANGE_LIMIT(429, "今日头像修改次数已达上限"),

    /** 头像不存在 */
    AVATAR_NOT_FOUND(400, "头像不存在"),

    /** 消息不存在 */
    MESSAGE_NOT_FOUND(404, "消息不存在"),

    /** 消息不可撤回（已超时或已撤回） */
    MESSAGE_REVOKE_NOT_ALLOWED(400, "该消息不可撤回（已超时或已撤回）"),

    /** 指定用户发送时接收人不能为空 */
    MESSAGE_RECEIVER_EMPTY(400, "指定用户发送时接收人不能为空");

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
