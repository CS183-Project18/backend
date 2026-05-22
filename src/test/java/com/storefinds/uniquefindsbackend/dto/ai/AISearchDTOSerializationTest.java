package com.storefinds.uniquefindsbackend.dto.ai;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AISearchDTOSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildIndexRequestIncludesImageUrls() throws Exception {
        IndexPostData post = new IndexPostData(1L, "Test Title", "Test Description", List.of("https://example.com/1.jpg"));
        BuildIndexRequest request = new BuildIndexRequest(List.of(post));

        String json = objectMapper.writeValueAsString(request);
        BuildIndexRequest deserialized = objectMapper.readValue(json, BuildIndexRequest.class);

        assertTrue(json.contains("\"image_urls\""));
        assertEquals(List.of("https://example.com/1.jpg"), deserialized.getPosts().get(0).getImageUrls());
    }

    @Test
    void aiSearchResponseDeserializesPostIds() throws Exception {
        String json = "{\"code\":200,\"data\":{\"post_ids\":[1,2,3],\"cached\":true},\"message\":\"success\"}";

        AISearchResponse response = objectMapper.readValue(json, AISearchResponse.class);

        assertEquals(200, response.getCode());
        assertEquals(List.of(1L, 2L, 3L), response.getData().getPostIds());
        assertTrue(response.getData().getCached());
    }

    @Test
    void buildIndexResponseDeserializesCoreFields() throws Exception {
        String json = "{\"code\":200,\"data\":{\"success\":true,\"message\":\"indices rebuilt\",\"count\":3},\"message\":\"success\"}";

        BuildIndexResponse response = objectMapper.readValue(json, BuildIndexResponse.class);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(true, response.getData().getSuccess());
        assertEquals(3, response.getData().getCount());
    }

    @Test
    void healthCheckResponseDeserializesCoreFields() throws Exception {
        String json = "{\"code\":200,\"data\":{\"status\":\"healthy\",\"service\":\"Unique Finds AI Search\",\"timestamp\":\"2026-05-22T10:30:00\"},\"message\":\"success\"}";

        HealthCheckResponse response = objectMapper.readValue(json, HealthCheckResponse.class);

        assertEquals("healthy", response.getData().getStatus());
        assertEquals("Unique Finds AI Search", response.getData().getService());
        assertEquals("2026-05-22T10:30:00", response.getData().getTimestamp());
    }

    @Test
    void errorResponseAllowsNullData() throws Exception {
        String json = "{\"code\":400,\"data\":null,\"message\":\"Invalid request\"}";

        AISearchResponse response = objectMapper.readValue(json, AISearchResponse.class);

        assertEquals(400, response.getCode());
        assertEquals("Invalid request", response.getMessage());
        assertNull(response.getData());
    }
}
