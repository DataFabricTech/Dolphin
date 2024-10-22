package com.mobigen.dolphin.exception;

import com.mobigen.dolphin.dto.response.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Pattern;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UncategorizedSQLException.class)
    protected ResponseEntity<MessageDto> handleUncategorizedSQLException(UncategorizedSQLException e) {
        var message = e.getSQLException().getCause().getMessage();
        log.error(message, e);
        ErrorCode errorCode;
        if (Pattern.matches(".+\\sColumn name '.+' specified more than once", message)) {
            errorCode = ErrorCode.INVALID_SQL_DUPLICATED_COLUMNS;
        } else {
            errorCode = ErrorCode.INVALID_SQL;
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageDto(errorCode.getStatus(), errorCode.getMessage() + e.getSQLException().getCause().getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<MessageDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var message = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            log.error(fieldError.getDefaultMessage());
            message.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append("\n");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageDto(ErrorCode.VALIDATION_ERROR.getStatus(), ErrorCode.VALIDATION_ERROR.getMessage() + message));
    }

    @ExceptionHandler(DolphinException.class)
    protected ResponseEntity<?> handleDolphinException(DolphinException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageDto(e.getErrorCode().getStatus(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageDto(ErrorCode.INTERNAL_ERROR.getStatus(), ErrorCode.INTERNAL_ERROR.getMessage() + e.getMessage()));
    }
}
