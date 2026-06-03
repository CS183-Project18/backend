package com.storefinds.uniquefindsbackend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Validate AI service exception hierarchy behavior.
 *
 * Unit tests for AI Service exception hierarchy.
 * Tests verify that exceptions are properly constructed with status codes,
 * messages, and causes, and that the inheritance hierarchy is correct.
 */
class AIServiceExceptionTest {

    @Test
    void aiServiceExceptionConstructorWithStatusCodeAndMessage() {
        AIServiceException exception = new AIServiceException(500, "Internal server error");

        assertEquals(500, exception.getStatusCode());
        assertEquals("Internal server error", exception.getServiceMessage());
        assertEquals("Internal server error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void aiServiceExceptionConstructorWithStatusCodeMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        AIServiceException exception = new AIServiceException(503, "Service unavailable", cause);

        assertEquals(503, exception.getStatusCode());
        assertEquals("Service unavailable", exception.getServiceMessage());
        assertEquals("Service unavailable", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void aiServiceExceptionExtendsRuntimeException() {
        AIServiceException exception = new AIServiceException(500, "Error");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void aiServiceTimeoutExceptionConstructorWithStatusCodeAndMessage() {
        AIServiceTimeoutException exception = new AIServiceTimeoutException(408, "Request timeout");

        assertEquals(408, exception.getStatusCode());
        assertEquals("Request timeout", exception.getServiceMessage());
        assertEquals("Request timeout", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void aiServiceTimeoutExceptionConstructorWithStatusCodeMessageAndCause() {
        Throwable cause = new java.net.SocketTimeoutException("Read timed out");
        AIServiceTimeoutException exception = new AIServiceTimeoutException(504, "Gateway timeout", cause);

        assertEquals(504, exception.getStatusCode());
        assertEquals("Gateway timeout", exception.getServiceMessage());
        assertEquals("Gateway timeout", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void aiServiceTimeoutExceptionExtendsAIServiceException() {
        AIServiceTimeoutException exception = new AIServiceTimeoutException(408, "Timeout");

        assertTrue(exception instanceof AIServiceException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void aiServiceUnavailableExceptionConstructorWithStatusCodeAndMessage() {
        AIServiceUnavailableException exception = new AIServiceUnavailableException(503, "Service unavailable");

        assertEquals(503, exception.getStatusCode());
        assertEquals("Service unavailable", exception.getServiceMessage());
        assertEquals("Service unavailable", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void aiServiceUnavailableExceptionConstructorWithStatusCodeMessageAndCause() {
        Throwable cause = new java.net.ConnectException("Connection refused");
        AIServiceUnavailableException exception = new AIServiceUnavailableException(503, "Cannot connect to AI service", cause);

        assertEquals(503, exception.getStatusCode());
        assertEquals("Cannot connect to AI service", exception.getServiceMessage());
        assertEquals("Cannot connect to AI service", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void aiServiceUnavailableExceptionExtendsAIServiceException() {
        AIServiceUnavailableException exception = new AIServiceUnavailableException(503, "Unavailable");

        assertTrue(exception instanceof AIServiceException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void aiServiceParseExceptionConstructorWithStatusCodeAndMessage() {
        AIServiceParseException exception = new AIServiceParseException(200, "Failed to parse response JSON");

        assertEquals(200, exception.getStatusCode());
        assertEquals("Failed to parse response JSON", exception.getServiceMessage());
        assertEquals("Failed to parse response JSON", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void aiServiceParseExceptionConstructorWithStatusCodeMessageAndCause() {
        Throwable cause = new com.fasterxml.jackson.core.JsonParseException(null, "Unexpected character");
        AIServiceParseException exception = new AIServiceParseException(200, "Malformed JSON response", cause);

        assertEquals(200, exception.getStatusCode());
        assertEquals("Malformed JSON response", exception.getServiceMessage());
        assertEquals("Malformed JSON response", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void aiServiceParseExceptionExtendsAIServiceException() {
        AIServiceParseException exception = new AIServiceParseException(200, "Parse error");

        assertTrue(exception instanceof AIServiceException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exceptionHierarchyIsCorrect() {
        // Verify that all subclasses can be caught as AIServiceException
        AIServiceException timeout = new AIServiceTimeoutException(408, "Timeout");
        AIServiceException unavailable = new AIServiceUnavailableException(503, "Unavailable");
        AIServiceException parse = new AIServiceParseException(200, "Parse error");

        assertTrue(timeout instanceof AIServiceException);
        assertTrue(unavailable instanceof AIServiceException);
        assertTrue(parse instanceof AIServiceException);

        // Verify that subclasses are distinct types
        assertFalse(timeout instanceof AIServiceUnavailableException);
        assertFalse(timeout instanceof AIServiceParseException);
        assertFalse(unavailable instanceof AIServiceTimeoutException);
        assertFalse(unavailable instanceof AIServiceParseException);
        assertFalse(parse instanceof AIServiceTimeoutException);
        assertFalse(parse instanceof AIServiceUnavailableException);
    }

    @Test
    void statusCodeAndMessageAreAccessibleFromSubclasses() {
        AIServiceException timeout = new AIServiceTimeoutException(408, "Timeout message");
        AIServiceException unavailable = new AIServiceUnavailableException(503, "Unavailable message");
        AIServiceException parse = new AIServiceParseException(200, "Parse message");

        assertEquals(408, timeout.getStatusCode());
        assertEquals("Timeout message", timeout.getServiceMessage());

        assertEquals(503, unavailable.getStatusCode());
        assertEquals("Unavailable message", unavailable.getServiceMessage());

        assertEquals(200, parse.getStatusCode());
        assertEquals("Parse message", parse.getServiceMessage());
    }
}
