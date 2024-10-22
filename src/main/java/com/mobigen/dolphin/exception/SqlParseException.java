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
public class SqlParseException extends DolphinException {
    public SqlParseException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
    }
}
