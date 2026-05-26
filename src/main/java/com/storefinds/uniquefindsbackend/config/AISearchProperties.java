package com.storefinds.uniquefindsbackend.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for AI Search Service integration.
 * Maps to application.yml properties under the "app.ai" prefix.
 */
@ConfigurationProperties(prefix = "app.ai")
@Validated
public class AISearchProperties {

    /**
     * Base URL of the AI Search Service.
     * Example: http://localhost:8000
     */
    @NotBlank(message = "AI Search base URL must not be blank")
    private String baseUrl = "http://localhost:8000";

    /**
     * Base URL used by ai-search to fetch backend-hosted uploaded images during index building.
     * This may differ from the browser-facing public base URL in Docker/container environments.
     */
    @NotBlank(message = "AI asset base URL must not be blank")
    private String assetBaseUrl = "http://localhost:8080";

    /**
     * Connection timeout in milliseconds for establishing connection to AI service.
     * Default: 5000ms (5 seconds)
     */
    @Min(value = 1, message = "Connection timeout must be at least 1ms")
    private int connectionTimeout = 5000;

    /**
     * Read timeout in milliseconds for search operations (semantic and image search).
     * Default: 10000ms (10 seconds)
     */
    @Min(value = 1, message = "Read timeout must be at least 1ms")
    private int readTimeout = 10000;

    /**
     * Read timeout in milliseconds for index build operations.
     * Index building typically takes longer than search operations.
     * Default: 30000ms (30 seconds)
     */
    @Min(value = 1, message = "Index build timeout must be at least 1ms")
    private int indexBuildTimeout = 30000;

    /**
     * Maximum number of retry attempts for transient failures.
     * Default: 2 retries
     */
    @Min(value = 0, message = "Max retries must be at least 0")
    private int maxRetries = 2;

    /**
     * Circuit breaker configuration properties.
     */
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getAssetBaseUrl() {
        return assetBaseUrl;
    }

    public void setAssetBaseUrl(String assetBaseUrl) {
        this.assetBaseUrl = assetBaseUrl;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getIndexBuildTimeout() {
        return indexBuildTimeout;
    }

    public void setIndexBuildTimeout(int indexBuildTimeout) {
        this.indexBuildTimeout = indexBuildTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Circuit breaker configuration for preventing cascading failures.
     */
    public static class CircuitBreaker {

        /**
         * Number of consecutive failures before opening the circuit.
         * Default: 5 failures
         */
        @Min(value = 1, message = "Failure threshold must be at least 1")
        private int failureThreshold = 5;

        /**
         * Duration in milliseconds to wait before attempting to close an open circuit.
         * Default: 60000ms (60 seconds)
         */
        @Min(value = 1, message = "Wait duration must be at least 1ms")
        private long waitDuration = 60000;

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public long getWaitDuration() {
            return waitDuration;
        }

        public void setWaitDuration(long waitDuration) {
            this.waitDuration = waitDuration;
        }
    }
}
