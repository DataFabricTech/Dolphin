package com.mobigen.dolphin.exception;

import lombok.Getter;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
public class DolphinException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public DolphinException(ErrorCode errorCode, String msg) {
        this.errorCode = errorCode;
        this.message = msg;
    }

    public String getMessage() {
        return errorCode.getMessage() + ": " + message;
    }
}
