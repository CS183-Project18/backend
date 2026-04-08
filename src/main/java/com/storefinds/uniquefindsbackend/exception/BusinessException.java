package com.storefinds.uniquefindsbackend.exception;

public class BusinessException extends RuntimeException {
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Create business exception with readable message.
     * Params:
     * - message: business error message
     * Returns: None
     * Throws: None
     */
    public BusinessException(String message) {
        super(message);
    }
}
