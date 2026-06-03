package com.storefinds.uniquefindsbackend.exception;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Represent AI service failures caused by malformed or unexpected response payloads.
 */
public class AIServiceParseException extends AIServiceException {

    /**
     * Create parse exception with status code and message.
     * 
     * @param statusCode HTTP status code (typically 200 with bad body, or 500)
     * @param serviceMessage descriptive parsing error message
     */
    public AIServiceParseException(int statusCode, String serviceMessage) {
        super(statusCode, serviceMessage);
    }

    /**
     * Create parse exception with status code, message, and cause.
     * 
     * @param statusCode HTTP status code (typically 200 with bad body, or 500)
     * @param serviceMessage descriptive parsing error message
     * @param cause the underlying parsing exception (e.g., JsonProcessingException)
     */
    public AIServiceParseException(int statusCode, String serviceMessage, Throwable cause) {
        super(statusCode, serviceMessage, cause);
    }
}
