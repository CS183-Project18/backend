package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.client.AISearchClient;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDiscoveryFacadeTest {

    @Mock
    private SqlPostSearchService sqlPostSearchService;

    @Mock
    private AISearchClient aiSearchClient;

    @Mock
    private PostMapper postMapper;

    @Test
    void keywordSearchFallsBackToSqlWhenAiFails() {
        PostSearchQuery query = new PostSearchQuery("lamp", "%lamp%", null, null, List.of(), null, null, "latest", false, 1, 20, 0);
        PageResponse<Post> sqlPage = new PageResponse<>();
        sqlPage.setTotal(1L);
        sqlPage.setPage(1);
        sqlPage.setPageSize(20);
        sqlPage.setItems(List.of(new Post()));

        when(aiSearchClient.isHealthy()).thenReturn(true);
        when(aiSearchClient.semanticSearch("lamp", 100))
                .thenThrow(new AIServiceUnavailableException(503, "down"));
        when(sqlPostSearchService.searchPublishedPosts(query)).thenReturn(sqlPage);

        DefaultDiscoveryFacade facade = new DefaultDiscoveryFacade(sqlPostSearchService, aiSearchClient, postMapper);
        PageResponse<Post> result = facade.searchPublishedPosts(query);

        assertEquals(1L, result.getTotal());
        verify(sqlPostSearchService).searchPublishedPosts(query);
    }

    @Test
    void keywordSearchPreservesAiCandidateOrderWhenSortIsImplicit() {
        Post first = new Post();
        first.setId(9L);
        Post second = new Post();
        second.setId(3L);

        PostSearchQuery query = new PostSearchQuery("lamp", "%lamp%", null, null, List.of(), null, null, "latest", false, 1, 20, 0);
        when(aiSearchClient.isHealthy()).thenReturn(true);
        when(aiSearchClient.semanticSearch("lamp", 100)).thenReturn(List.of(9L, 3L));
        when(postMapper.selectPublishedPostsByIds(List.of(9L, 3L), null, null, List.of(), null, null, "latest", true))
                .thenReturn(List.of(first, second));

        DefaultDiscoveryFacade facade = new DefaultDiscoveryFacade(sqlPostSearchService, aiSearchClient, postMapper);
        PageResponse<Post> result = facade.searchPublishedPosts(query);

        assertEquals(List.of(9L, 3L), result.getItems().stream().map(Post::getId).toList());
        verify(sqlPostSearchService, never()).searchPublishedPosts(query);
    }

    @Test
    void imageSearchThrowsUnavailableWhenAiHealthCheckFails() {
        MockMultipartFile file = new MockMultipartFile("file", "lamp.jpg", "image/jpeg", new byte[]{1, 2, 3});
        PostSearchQuery query = new PostSearchQuery(null, null, null, null, List.of(), null, null, "latest", false, 1, 20, 0);
        when(aiSearchClient.isHealthy()).thenReturn(false);

        DefaultDiscoveryFacade facade = new DefaultDiscoveryFacade(sqlPostSearchService, aiSearchClient, postMapper);

        assertThrows(AIServiceUnavailableException.class, () -> facade.searchPublishedPostsByImage(query, file));
    }
}
