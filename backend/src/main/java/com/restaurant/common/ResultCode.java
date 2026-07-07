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
    SERVER_ERROR(500, "服务器内部错误");

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
