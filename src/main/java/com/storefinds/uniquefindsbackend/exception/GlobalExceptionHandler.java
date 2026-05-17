package com.storefinds.uniquefindsbackend.exception;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Convert business exceptions to unified bad request response.
     * Params:
     * - ex: business exception instance
     * Returns:
     * - Result<Void>: standardized error payload
     * Throws: None
     */
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.error(resolveBusinessErrorCode(ex), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Convert request body validation errors to client-friendly response.
     * Params:
     * - ex: validation exception
     * Returns:
     * - Result<Void>: standardized validation error payload
     * Throws: None
     */
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "invalid request";
        return Result.error(ErrorCode.INVALID_ARGUMENT, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Handle constraint violations from query/path parameter validation.
     * Params:
     * - ex: constraint violation exception
     * Returns:
     * - Result<Void>: standardized validation error payload
     * Throws: None
     */
    public Result<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.error(ErrorCode.INVALID_ARGUMENT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleUnexpectedException(Exception ex) {
        log.error("unexpected server error", ex);
        return Result.error(ErrorCode.INTERNAL_ERROR, "internal server error");
    }

    private String resolveBusinessErrorCode(BusinessException ex) {
        if (ex.getCode() != null && !ErrorCode.BUSINESS_ERROR.equals(ex.getCode())) {
            return ex.getCode();
        }
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (message.contains("unauthorized")) {
            return ErrorCode.UNAUTHORIZED;
        }
        if (message.contains("not found")) {
            return ErrorCode.NOT_FOUND;
        }
        if (message.contains("already") || message.contains("duplicate")) {
            return ErrorCode.CONFLICT;
        }
        if (message.contains("cannot") || message.contains("only operate") || message.contains("no permission")) {
            return ErrorCode.FORBIDDEN;
        }
        if (message.contains("required")
                || message.contains("invalid")
                || message.contains("must be")
                || message.contains("too large")
                || message.contains("supported")) {
            return ErrorCode.INVALID_ARGUMENT;
        }
        if (message.contains("not available")
                || message.contains("not visible")
                || message.contains("not published")
                || message.contains("status")) {
            return ErrorCode.INVALID_STATE;
        }
        return ErrorCode.BUSINESS_ERROR;
    }
}
