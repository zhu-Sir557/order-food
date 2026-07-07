package com.restaurant.common;

import lombok.Getter;

/**
 * Business exception for domain-level errors.
 *
 * <p>Thrown when a business rule is violated. Caught by
 * {@link GlobalExceptionHandler} and converted to a structured error response.</p>
 */
@Getter
public class BizException extends RuntimeException {

    /** Error code */
    private final int code;

    /** Error message */
    private final String message;

    /**
     * Constructor with code and message.
     *
     * @param code    error code
     * @param message error message
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * Constructor with ResultCode enum.
     *
     * @param resultCode result code enum
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * Constructor with ResultCode enum and custom message.
     *
     * @param resultCode result code enum
     * @param message    custom error message
     */
    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * Constructor with message (defaults to 400).
     *
     * @param message error message
     */
    public BizException(String message) {
        super(message);
        this.code = ResultCode.PARAM_ERROR.getCode();
        this.message = message;
    }
}
