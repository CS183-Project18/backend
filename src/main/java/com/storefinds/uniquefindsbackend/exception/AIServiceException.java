package com.storefinds.uniquefindsbackend.exception;

/**
 * Base exception for AI Search Service related errors.
 * Extends RuntimeException to allow unchecked exception handling.
 * 
 * This exception hierarchy is used by AISearchClient to represent
 * various failure modes when communicating with the AI Search Service.
 */
public class AIServiceException extends RuntimeException {

    private final int statusCode;
    private final String serviceMessage;

    /**
     * Create AI service exception with status code and service message.
     * 
     * @param statusCode HTTP status code or error code from AI service
     * @param serviceMessage descriptive error message from AI service
     */
    public AIServiceException(int statusCode, String serviceMessage) {
        super(serviceMessage);
        this.statusCode = statusCode;
        this.serviceMessage = serviceMessage;
    }

    /**
     * Create AI service exception with status code, service message, and cause.
     * 
     * @param statusCode HTTP status code or error code from AI service
     * @param serviceMessage descriptive error message from AI service
     * @param cause the underlying cause of this exception
     */
    public AIServiceException(int statusCode, String serviceMessage, Throwable cause) {
        super(serviceMessage, cause);
        this.statusCode = statusCode;
        this.serviceMessage = serviceMessage;
    }

    /**
     * Get the HTTP status code or error code from AI service.
     * 
     * @return status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get the descriptive error message from AI service.
     * 
     * @return service message
     */
    public String getServiceMessage() {
        return serviceMessage;
    }
}
