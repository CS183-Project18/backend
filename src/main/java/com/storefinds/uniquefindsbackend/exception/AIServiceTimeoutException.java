package com.storefinds.uniquefindsbackend.exception;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Represent AI search requests that exceed the configured timeout window.
 */
public class AIServiceTimeoutException extends AIServiceException {

    /**
     * Create timeout exception with status code and message.
     * 
     * @param statusCode HTTP status code (typically 408 or 504)
     * @param serviceMessage descriptive timeout message
     */
    public AIServiceTimeoutException(int statusCode, String serviceMessage) {
        super(statusCode, serviceMessage);
    }

    /**
     * Create timeout exception with status code, message, and cause.
     * 
     * @param statusCode HTTP status code (typically 408 or 504)
     * @param serviceMessage descriptive timeout message
     * @param cause the underlying timeout exception
     */
    public AIServiceTimeoutException(int statusCode, String serviceMessage, Throwable cause) {
        super(statusCode, serviceMessage, cause);
    }
}
