package com.storefinds.uniquefindsbackend.client;

import com.storefinds.uniquefindsbackend.config.AISearchProperties;
import com.storefinds.uniquefindsbackend.dto.ai.AISearchResponse;
import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexRequest;
import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexResponse;
import com.storefinds.uniquefindsbackend.dto.ai.HealthCheckResponse;
import com.storefinds.uniquefindsbackend.dto.ai.IndexPostData;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import com.storefinds.uniquefindsbackend.exception.AIServiceParseException;
import com.storefinds.uniquefindsbackend.exception.AIServiceTimeoutException;
import com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Call the internal ai-search service with timeout handling, response parsing, and lightweight failure backoff.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AISearchClientImpl implements AISearchClient {

    private static final Logger log = LoggerFactory.getLogger(AISearchClientImpl.class);
    private static final long HEALTH_CACHE_MILLIS = Duration.ofSeconds(30).toMillis();

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final AISearchProperties aiSearchProperties;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong circuitOpenedAt = new AtomicLong(0L);
    private final AtomicLong lastHealthCheckAt = new AtomicLong(0L);
    private final AtomicLong lastKnownHealthValue = new AtomicLong(0L);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Inject HTTP client builder, JSON mapper, and ai-search configuration.
     * Params:
     * - restClientBuilder: Spring RestClient builder
     * - objectMapper: shared JSON mapper
     * - aiSearchProperties: ai-search integration configuration
     * Returns: None
     * Throws: None
     */
    public AISearchClientImpl(RestClient.Builder restClientBuilder,
                              ObjectMapper objectMapper,
                              AISearchProperties aiSearchProperties) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
        this.aiSearchProperties = aiSearchProperties;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Check whether the ai-search service is healthy while caching recent health results and respecting circuit cooldown.
     * Params: None
     * Returns:
     * - boolean: true when ai-search is healthy, otherwise false
     * Throws: None
     */
    public boolean isHealthy() {
        if (isCircuitOpen()) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - lastHealthCheckAt.get() < HEALTH_CACHE_MILLIS) {
            return lastKnownHealthValue.get() == 1L;
        }
        try {
            String body = createRestClient(aiSearchProperties.getReadTimeout())
                    .get()
                    .uri("/health")
                    .retrieve()
                    .body(String.class);
            HealthCheckResponse response = parseBody(body, HealthCheckResponse.class);
            boolean healthy = response != null
                    && response.getCode() != null
                    && response.getCode() == 200
                    && response.getData() != null
                    && "healthy".equalsIgnoreCase(response.getData().getStatus());
            lastHealthCheckAt.set(now);
            lastKnownHealthValue.set(healthy ? 1L : 0L);
            if (healthy) {
                recordSuccess();
            } else {
                recordFailure();
            }
            return healthy;
        } catch (AIServiceException ex) {
            log.debug("ai-search health check failed", ex);
            lastHealthCheckAt.set(now);
            lastKnownHealthValue.set(0L);
            return false;
        }
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Execute one semantic search request against the ai-search service and return ordered post ids.
     * Params:
     * - query: normalized keyword query
     * - topK: maximum result count
     * Returns:
     * - List<Long>: ordered matched post ids
     * Throws:
     * - AIServiceException: when the ai-search request fails or returns malformed data
     */
    public List<Long> semanticSearch(String query, int topK) throws AIServiceException {
        validateQuery(query, topK);
        if (isCircuitOpen()) {
            throw new AIServiceUnavailableException(503, "ai-search service is temporarily unavailable");
        }
        try {
            String body = createRestClient(aiSearchProperties.getReadTimeout())
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/semantic_search")
                            .queryParam("q", query)
                            .queryParam("top_k", topK)
                            .build())
                    .retrieve()
                    .body(String.class);
            AISearchResponse response = parseBody(body, AISearchResponse.class);
            List<Long> postIds = extractPostIds(response);
            recordSuccess();
            return postIds;
        } catch (RestClientException ex) {
            throw mapRestClientException(ex, "semantic search");
        } catch (AIServiceException ex) {
            recordFailure();
            throw ex;
        }
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Execute one image search request against the ai-search service and return ordered post ids.
     * Params:
     * - imageFile: uploaded image file
     * - topK: maximum result count
     * Returns:
     * - List<Long>: ordered matched post ids
     * Throws:
     * - AIServiceException: when the ai-search request fails or returns malformed data
     */
    public List<Long> imageSearch(MultipartFile imageFile, int topK) throws AIServiceException {
        if (imageFile == null) {
            throw new IllegalArgumentException("imageFile must not be null");
        }
        validateTopK(topK);
        if (isCircuitOpen()) {
            throw new AIServiceUnavailableException(503, "ai-search service is temporarily unavailable");
        }
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", toMultipartResource(imageFile));
            body.add("top_k", String.valueOf(topK));
            String responseBody = createRestClient(aiSearchProperties.getReadTimeout())
                    .post()
                    .uri("/image_search")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            AISearchResponse response = parseBody(responseBody, AISearchResponse.class);
            List<Long> postIds = extractPostIds(response);
            recordSuccess();
            return postIds;
        } catch (IOException ex) {
            recordFailure();
            throw new AIServiceException(500, "failed to read uploaded image file", ex);
        } catch (RestClientException ex) {
            throw mapRestClientException(ex, "image search");
        } catch (AIServiceException ex) {
            recordFailure();
            throw ex;
        }
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Trigger one full ai-search index rebuild with the provided published post payload.
     * Params:
     * - posts: published post payload to index
     * Returns:
     * - BuildIndexResponse: ai-search build response
     * Throws:
     * - AIServiceException: when the ai-search request fails or returns malformed data
     */
    public BuildIndexResponse buildIndex(List<IndexPostData> posts) throws AIServiceException {
        if (posts == null) {
            throw new IllegalArgumentException("posts must not be null");
        }
        if (isCircuitOpen()) {
            throw new AIServiceUnavailableException(503, "ai-search service is temporarily unavailable");
        }
        try {
            String body = createRestClient(aiSearchProperties.getIndexBuildTimeout())
                    .post()
                    .uri("/build_index")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new BuildIndexRequest(posts))
                    .retrieve()
                    .body(String.class);
            BuildIndexResponse response = parseBody(body, BuildIndexResponse.class);
            if (response == null || response.getCode() == null) {
                recordFailure();
                throw new AIServiceParseException(500, "build index response is missing required fields");
            }
            if (response.getCode() != 200) {
                recordFailure();
                throw new AIServiceException(response.getCode(), response.getMessage());
            }
            recordSuccess();
            return response;
        } catch (RestClientException ex) {
            throw mapRestClientException(ex, "build index");
        } catch (AIServiceException ex) {
            recordFailure();
            throw ex;
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Build one RestClient with the configured base URL and timeout pair for the current AI request type.
     * Params:
     * - readTimeout: read timeout in milliseconds
     * Returns:
     * - RestClient: configured RestClient instance
     * Throws: None
     */
    private RestClient createRestClient(int readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(aiSearchProperties.getConnectionTimeout());
        requestFactory.setReadTimeout(readTimeout);
        return restClientBuilder
                .baseUrl(aiSearchProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Parse one JSON response body into the target DTO type and convert malformed payloads into AI parse exceptions.
     * Params:
     * - body: raw JSON response body
     * - targetType: target DTO class
     * Returns:
     * - T: parsed DTO instance
     * Throws:
     * - AIServiceParseException: when the body cannot be parsed as the target DTO
     */
    private <T> T parseBody(String body, Class<T> targetType) {
        if (body == null || body.isBlank()) {
            recordFailure();
            throw new AIServiceParseException(500, "ai-search response body is empty");
        }
        try {
            return objectMapper.readValue(body, targetType);
        } catch (JacksonException ex) {
            recordFailure();
            throw new AIServiceParseException(500, "failed to parse ai-search response", ex);
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Extract post ids from one ai-search response and validate the stable response shape expected by the backend.
     * Params:
     * - response: parsed ai-search response
     * Returns:
     * - List<Long>: response post id list
     * Throws:
     * - AIServiceException: when the response code or payload shape is invalid
     */
    private List<Long> extractPostIds(AISearchResponse response) {
        if (response == null || response.getCode() == null) {
            recordFailure();
            throw new AIServiceParseException(500, "ai-search response is missing required fields");
        }
        if (response.getCode() != 200) {
            recordFailure();
            throw new AIServiceException(response.getCode(), response.getMessage());
        }
        if (response.getData() == null || response.getData().getPostIds() == null) {
            recordFailure();
            throw new AIServiceParseException(500, "ai-search response data is missing post_ids");
        }
        return response.getData().getPostIds();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Convert one uploaded multipart file into a Spring ByteArrayResource with a stable filename for multipart forwarding.
     * Params:
     * - imageFile: uploaded image file
     * Returns:
     * - ByteArrayResource: multipart body resource
     * Throws:
     * - IOException: when the uploaded file cannot be read
     */
    private ByteArrayResource toMultipartResource(MultipartFile imageFile) throws IOException {
        return new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        };
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Map one low-level RestClient exception into the project-level AI exception hierarchy and record failure state.
     * Params:
     * - ex: source RestClient exception
     * - operation: readable operation description
     * Returns:
     * - AIServiceException: mapped project-level AI exception
     * Throws: None
     */
    private AIServiceException mapRestClientException(RestClientException ex, String operation) {
        recordFailure();
        if (ex instanceof RestClientResponseException responseException) {
            return new AIServiceException(responseException.getStatusCode().value(),
                    "ai-search " + operation + " failed: " + responseException.getResponseBodyAsString(),
                    ex);
        }
        if (ex instanceof ResourceAccessException resourceAccessException) {
            Throwable cause = resourceAccessException.getCause();
            if (cause instanceof SocketTimeoutException) {
                return new AIServiceTimeoutException(504, "ai-search " + operation + " timed out", ex);
            }
            return new AIServiceUnavailableException(503, "ai-search " + operation + " is unavailable", ex);
        }
        return new AIServiceException(500, "ai-search " + operation + " failed", ex);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Validate one semantic query before calling the ai-search service.
     * Params:
     * - query: raw semantic query
     * - topK: requested maximum result count
     * Returns: None
     * Throws:
     * - IllegalArgumentException: when query or topK is invalid
     */
    private void validateQuery(String query, int topK) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query must not be blank");
        }
        validateTopK(topK);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Validate one requested AI top-k value before forwarding it to ai-search.
     * Params:
     * - topK: requested maximum result count
     * Returns: None
     * Throws:
     * - IllegalArgumentException: when topK is less than 1
     */
    private void validateTopK(int topK) {
        if (topK < 1) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Reset lightweight failure tracking after one successful ai-search interaction.
     * Params: None
     * Returns: None
     * Throws: None
     */
    private void recordSuccess() {
        consecutiveFailures.set(0);
        circuitOpenedAt.set(0L);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Increase lightweight failure tracking and open the temporary circuit when repeated failures reach the configured threshold.
     * Params: None
     * Returns: None
     * Throws: None
     */
    private void recordFailure() {
        int failureCount = consecutiveFailures.incrementAndGet();
        if (failureCount >= aiSearchProperties.getCircuitBreaker().getFailureThreshold()) {
            circuitOpenedAt.compareAndSet(0L, System.currentTimeMillis());
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Determine whether repeated recent AI failures should short-circuit new outbound AI calls for a cooldown window.
     * Params: None
     * Returns:
     * - boolean: true when the cooldown circuit remains open, otherwise false
     * Throws: None
     */
    private boolean isCircuitOpen() {
        long openedAt = circuitOpenedAt.get();
        if (openedAt == 0L) {
            return false;
        }
        long waitDuration = aiSearchProperties.getCircuitBreaker().getWaitDuration();
        if (System.currentTimeMillis() - openedAt < waitDuration) {
            return true;
        }
        circuitOpenedAt.set(0L);
        consecutiveFailures.set(0);
        return false;
    }
}
