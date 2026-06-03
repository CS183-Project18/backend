package com.storefinds.uniquefindsbackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Validate AI search configuration binding behavior.
 *
 * Unit tests for AISearchProperties configuration class.
 * Validates that configuration properties are correctly loaded from application.yml.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "app.ai.base-url=http://test-ai-service:8000",
        "app.ai.asset-base-url=http://test-backend:8080",
        "app.ai.connection-timeout=3000",
        "app.ai.read-timeout=8000",
        "app.ai.index-build-timeout=25000",
        "app.ai.max-retries=3",
        "app.ai.circuit-breaker.failure-threshold=10",
        "app.ai.circuit-breaker.wait-duration=30000"
})
class AISearchPropertiesTest {

    @Autowired
    private AISearchProperties aiSearchProperties;

    @Test
    void testPropertiesAreLoadedCorrectly() {
        // Verify main properties
        assertThat(aiSearchProperties.getBaseUrl()).isEqualTo("http://test-ai-service:8000");
        assertThat(aiSearchProperties.getAssetBaseUrl()).isEqualTo("http://test-backend:8080");
        assertThat(aiSearchProperties.getConnectionTimeout()).isEqualTo(3000);
        assertThat(aiSearchProperties.getReadTimeout()).isEqualTo(8000);
        assertThat(aiSearchProperties.getIndexBuildTimeout()).isEqualTo(25000);
        assertThat(aiSearchProperties.getMaxRetries()).isEqualTo(3);

        // Verify circuit breaker properties
        assertThat(aiSearchProperties.getCircuitBreaker()).isNotNull();
        assertThat(aiSearchProperties.getCircuitBreaker().getFailureThreshold()).isEqualTo(10);
        assertThat(aiSearchProperties.getCircuitBreaker().getWaitDuration()).isEqualTo(30000);
    }

    @Test
    void testDefaultValuesAreApplied() {
        // Create a new instance to test default values
        AISearchProperties defaultProperties = new AISearchProperties();

        assertThat(defaultProperties.getBaseUrl()).isEqualTo("http://localhost:8000");
        assertThat(defaultProperties.getAssetBaseUrl()).isEqualTo("http://localhost:8080");
        assertThat(defaultProperties.getConnectionTimeout()).isEqualTo(5000);
        assertThat(defaultProperties.getReadTimeout()).isEqualTo(10000);
        assertThat(defaultProperties.getIndexBuildTimeout()).isEqualTo(30000);
        assertThat(defaultProperties.getMaxRetries()).isEqualTo(2);

        assertThat(defaultProperties.getCircuitBreaker()).isNotNull();
        assertThat(defaultProperties.getCircuitBreaker().getFailureThreshold()).isEqualTo(5);
        assertThat(defaultProperties.getCircuitBreaker().getWaitDuration()).isEqualTo(60000);
    }

    @Test
    void testCircuitBreakerNestedProperties() {
        AISearchProperties.CircuitBreaker circuitBreaker = aiSearchProperties.getCircuitBreaker();

        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getFailureThreshold()).isGreaterThan(0);
        assertThat(circuitBreaker.getWaitDuration()).isGreaterThan(0);
    }
}
