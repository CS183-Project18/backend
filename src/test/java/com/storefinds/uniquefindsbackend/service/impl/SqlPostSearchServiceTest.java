package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/**
 * Author: Shuying Liang
 * Date: 2026-05-22
 * Purpose: Verify SQL discovery behavior that supports product demo stability.
 * Params: None
 * Returns: None
 * Throws: None
 */
class SqlPostSearchServiceTest {

    @Mock
    private PostMapper postMapper;

    @Test
    void dailyTrendingFallsBackToWeeklyWhenDailyHasNoResults() {
        TrendingPostsQuery query = new TrendingPostsQuery("daily", LocalDateTime.now().minusDays(1), 1, 10, 0);
        Post post = new Post();
        post.setId(8L);
        when(postMapper.countTrendingPosts(org.mockito.ArgumentMatchers.any(LocalDateTime.class))).thenReturn(0L, 1L);
        when(postMapper.selectTrendingPosts(org.mockito.ArgumentMatchers.any(LocalDateTime.class), eq(0), eq(10))).thenReturn(List.of(post));

        SqlPostSearchService service = new SqlPostSearchService(postMapper);
        PageResponse<Post> result = service.getTrendingPosts(query);

        assertEquals(1L, result.getTotal());
        assertEquals("weekly", result.getMetadata().get("effectiveWindow"));
        assertEquals(true, result.getMetadata().get("fallbackApplied"));
    }
}
