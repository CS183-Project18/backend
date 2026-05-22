package com.storefinds.uniquefindsbackend.exception;

/**
 * Exception thrown when AI Search Service is not reachable or unavailable.
 * 
 * This can occur when:
 * - Service is not running or has crashed
 * - Network connectivity issues prevent reaching the service
 * - Service returns 503 Service Unavailable status
 * - DNS resolution fails for the service hostname
 * 
 * When this exception is thrown:
 * - Text search operations should fall back to SQL-based search
 * - Image search operations should return empty results with descriptive message
 * - Index rebuild operations should log failure without blocking
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
