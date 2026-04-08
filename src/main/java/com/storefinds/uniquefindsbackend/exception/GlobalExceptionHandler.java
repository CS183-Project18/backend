package com.storefinds.uniquefindsbackend.exception;

import com.storefinds.uniquefindsbackend.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        return Result.error(ex.getMessage());
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
        return Result.error(message);
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
        return Result.error(ex.getMessage());
    }
}
