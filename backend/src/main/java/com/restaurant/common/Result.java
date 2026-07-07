package com.restaurant.common;

import lombok.Data;

/**
 * Unified API response wrapper.
 *
 * <p>All API endpoints return this structure:
 * <pre>{@code
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 * }</pre></p>
 *
 * @param <T> the type of data payload
 */
@Data
public class Result<T> {

    /** Response code */
    private int code;

    /** Response message */
    private String message;

    /** Response data */
    private T data;

    /**
     * Default constructor.
     */
    public Result() {
    }

    /**
     * Full constructor.
     *
     * @param code    response code
     * @param message response message
     * @param data    response data
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * Success response with data.
     *
     * @param data response data
     * @param <T>  data type
     * @return success result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * Success response without data.
     *
     * @param <T> data type
     * @return success result
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * Success response with custom message and data.
     *
     * @param message custom message
     * @param data    response data
     * @param <T>     data type
     * @return success result
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * Error response with code and message.
     *
     * @param code    error code
     * @param message error message
     * @param <T>     data type
     * @return error result
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * Error response with ResultCode enum.
     *
     * @param resultCode result code enum
     * @param <T>        data type
     * @return error result
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * Error response with message only (defaults to 400).
     *
     * @param message error message
     * @param <T>     data type
     * @return error result
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.PARAM_ERROR.getCode(), message, null);
    }
}
