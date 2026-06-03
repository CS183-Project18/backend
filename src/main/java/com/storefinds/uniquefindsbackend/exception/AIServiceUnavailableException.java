package com.storefinds.uniquefindsbackend.exception;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Represent connectivity or availability failures when the backend cannot reach ai-search.
 */
public class AIServiceUnavailableException extends AIServiceException {

    /**
     * Create unavailable exception with status code and message.
     * 
     * @param statusCode HTTP status code (typically 503)
     * @param serviceMessage descriptive unavailability message
     */
    public AIServiceUnavailableException(int statusCode, String serviceMessage) {
        super(statusCode, serviceMessage);
    }

    /**
     * Create unavailable exception with status code, message, and cause.
     * 
     * @param statusCode HTTP status code (typically 503)
     * @param serviceMessage descriptive unavailability message
     * @param cause the underlying connection or network exception
     */
    public AIServiceUnavailableException(int statusCode, String serviceMessage, Throwable cause) {
        super(statusCode, serviceMessage, cause);
    }
}
