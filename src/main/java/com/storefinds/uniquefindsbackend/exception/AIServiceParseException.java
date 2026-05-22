package com.storefinds.uniquefindsbackend.exception;

/**
 * Exception thrown when AI Search Service returns a response that cannot be parsed.
 * 
 * This can occur when:
 * - Response body contains malformed JSON
 * - Response JSON is missing required fields
 * - Response JSON has unexpected data types for fields
 * - Response encoding is not UTF-8 or is corrupted
 * 
 * When this exception is thrown:
 * - Text search operations should fall back to SQL-based search
 * - Image search operations should return empty results with descriptive message
 * - Index rebuild operations should log failure without blocking
 * 
 * The exception message should include details about what parsing failed
 * and ideally include a snippet of the problematic response for debugging.
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
