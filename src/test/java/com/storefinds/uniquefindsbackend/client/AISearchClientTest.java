package com.storefinds.uniquefindsbackend.client;

import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexResponse;
import com.storefinds.uniquefindsbackend.dto.ai.IndexPostData;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AISearchClient interface.
 * <p>
 * These tests verify the interface contract and method signatures.
 * Implementation-specific tests should be in the implementation class test file.
 * </p>
 *
 * @author Shuying Liang
 * @version 1.0
 * @since 2026-05-15
 */
class AISearchClientTest {

    /**
     * Test that the interface defines the isHealthy method with correct signature.
     */
    @Test
    void testIsHealthyMethodExists() {
        AISearchClient mockClient = mock(AISearchClient.class);
        when(mockClient.isHealthy()).thenReturn(true);
        
        boolean result = mockClient.isHealthy();
        
        assertTrue(result);
        verify(mockClient).isHealthy();
    }

    /**
     * Test that the interface defines the semanticSearch method with correct signature.
     */
    @Test
    void testSemanticSearchMethodExists() throws AIServiceException {
        AISearchClient mockClient = mock(AISearchClient.class);
        List<Long> expectedIds = List.of(1L, 2L, 3L);
        when(mockClient.semanticSearch("test query", 20)).thenReturn(expectedIds);
        
        List<Long> result = mockClient.semanticSearch("test query", 20);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedIds, result);
        verify(mockClient).semanticSearch("test query", 20);
    }

    /**
     * Test that the interface defines the imageSearch method with correct signature.
     */
    @Test
    void testImageSearchMethodExists() throws AIServiceException {
        AISearchClient mockClient = mock(AISearchClient.class);
        MultipartFile mockFile = mock(MultipartFile.class);
        List<Long> expectedIds = List.of(4L, 5L, 6L);
        when(mockClient.imageSearch(mockFile, 20)).thenReturn(expectedIds);
        
        List<Long> result = mockClient.imageSearch(mockFile, 20);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedIds, result);
        verify(mockClient).imageSearch(mockFile, 20);
    }

    /**
     * Test that the interface defines the buildIndex method with correct signature.
     */
    @Test
    void testBuildIndexMethodExists() throws AIServiceException {
        AISearchClient mockClient = mock(AISearchClient.class);
        List<IndexPostData> posts = new ArrayList<>();
        posts.add(new IndexPostData(
                1L,
                "Test Post",
                "Test Description",
                List.of("https://example.com/test.jpg"),
                List.of("desk-lamp"),
                "Lighting",
                "Studio Market",
                "Dublin"
        ));
        
        BuildIndexResponse mockResponse = mock(BuildIndexResponse.class);
        when(mockClient.buildIndex(posts)).thenReturn(mockResponse);
        
        BuildIndexResponse result = mockClient.buildIndex(posts);
        
        assertNotNull(result);
        verify(mockClient).buildIndex(posts);
    }

    /**
     * Test that semanticSearch can throw AIServiceException.
     */
    @Test
    void testSemanticSearchCanThrowAIServiceException() {
        AISearchClient mockClient = mock(AISearchClient.class);
        
        assertDoesNotThrow(() -> {
            when(mockClient.semanticSearch("test", 10))
                .thenThrow(new AIServiceException(500, "Internal error"));
        });
    }

    /**
     * Test that imageSearch can throw AIServiceException.
     */
    @Test
    void testImageSearchCanThrowAIServiceException() {
        AISearchClient mockClient = mock(AISearchClient.class);
        MultipartFile mockFile = mock(MultipartFile.class);
        
        assertDoesNotThrow(() -> {
            when(mockClient.imageSearch(mockFile, 10))
                .thenThrow(new AIServiceException(500, "Internal error"));
        });
    }

    /**
     * Test that buildIndex can throw AIServiceException.
     */
    @Test
    void testBuildIndexCanThrowAIServiceException() {
        AISearchClient mockClient = mock(AISearchClient.class);
        List<IndexPostData> posts = new ArrayList<>();
        
        assertDoesNotThrow(() -> {
            when(mockClient.buildIndex(posts))
                .thenThrow(new AIServiceException(500, "Internal error"));
        });
    }

    /**
     * Test that isHealthy returns false when service is unavailable.
     */
    @Test
    void testIsHealthyReturnsFalseWhenUnavailable() {
        AISearchClient mockClient = mock(AISearchClient.class);
        when(mockClient.isHealthy()).thenReturn(false);
        
        boolean result = mockClient.isHealthy();
        
        assertFalse(result);
    }

    /**
     * Test that semanticSearch returns empty list when no results found.
     */
    @Test
    void testSemanticSearchReturnsEmptyListWhenNoResults() throws AIServiceException {
        AISearchClient mockClient = mock(AISearchClient.class);
        when(mockClient.semanticSearch("nonexistent", 20)).thenReturn(List.of());
        
        List<Long> result = mockClient.semanticSearch("nonexistent", 20);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test that imageSearch returns empty list when no results found.
     */
    @Test
    void testImageSearchReturnsEmptyListWhenNoResults() throws AIServiceException {
        AISearchClient mockClient = mock(AISearchClient.class);
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockClient.imageSearch(mockFile, 20)).thenReturn(List.of());
        
        List<Long> result = mockClient.imageSearch(mockFile, 20);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
