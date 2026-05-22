package com.storefinds.uniquefindsbackend.exception;

/**
 * Exception thrown when a request to AI Search Service exceeds the configured timeout.
 * 
 * This can occur during:
 * - Connection timeout: unable to establish connection within timeout period
 * - Read timeout: connection established but response not received within timeout period
 * 
 * Timeout durations are configured per operation type:
 * - Search operations: typically 10 seconds
 * - Index build operations: typically 30 seconds
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
