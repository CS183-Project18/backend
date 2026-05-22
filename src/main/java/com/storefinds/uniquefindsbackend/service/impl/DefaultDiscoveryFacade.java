package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.client.AISearchClient;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import com.storefinds.uniquefindsbackend.exception.AIServiceUnavailableException;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.DiscoveryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Orchestrate AI-first discovery flows while preserving the existing SQL search fallback behavior.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class DefaultDiscoveryFacade implements DiscoveryFacade {

    private static final Logger log = LoggerFactory.getLogger(DefaultDiscoveryFacade.class);
    private static final int AI_SEARCH_TOP_K = 100;

    private final SqlPostSearchService sqlPostSearchService;
    private final AISearchClient aiSearchClient;
    private final PostMapper postMapper;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Inject the SQL discovery backend, AI search client, and post mapper used by discovery orchestration.
     * Params:
     * - sqlPostSearchService: current SQL discovery backend
     * - aiSearchClient: AI search integration client
     * - postMapper: post mapper for AI candidate retrieval
     * Returns: None
     * Throws: None
     */
    public DefaultDiscoveryFacade(SqlPostSearchService sqlPostSearchService,
                                  AISearchClient aiSearchClient,
                                  PostMapper postMapper) {
        this.sqlPostSearchService = sqlPostSearchService;
        this.aiSearchClient = aiSearchClient;
        this.postMapper = postMapper;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Execute one published-post search using AI semantic ranking first and SQL fallback on failures.
     * Params:
     * - query: normalized published-post search query
     * Returns:
     * - PageResponse<Post>: paginated matched post page
     * Throws: None
     */
    public PageResponse<Post> searchPublishedPosts(PostSearchQuery query) {
        if (query.keyword() == null) {
            return sqlPostSearchService.searchPublishedPosts(query);
        }
        if (!aiSearchClient.isHealthy()) {
            return sqlPostSearchService.searchPublishedPosts(query);
        }
        try {
            List<Long> postIds = aiSearchClient.semanticSearch(query.keyword(), AI_SEARCH_TOP_K);
            return buildAiOrderedPage(query, postIds);
        } catch (AIServiceException ex) {
            log.warn("ai semantic search failed, falling back to sql search: keyword={}", query.keyword(), ex);
            return sqlPostSearchService.searchPublishedPosts(query);
        }
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Execute one image-based published-post search and propagate AI unavailability so callers can return the agreed degraded response.
     * Params:
     * - query: normalized filter and pagination query
     * - file: uploaded image file
     * Returns:
     * - PageResponse<Post>: paginated matched post page
     * Throws: None
     */
    public PageResponse<Post> searchPublishedPostsByImage(PostSearchQuery query, MultipartFile file) {
        if (!aiSearchClient.isHealthy()) {
            throw new AIServiceUnavailableException(503, "ai-search service is temporarily unavailable");
        }
        List<Long> postIds = aiSearchClient.imageSearch(file, AI_SEARCH_TOP_K);
        return buildAiOrderedPage(query, postIds);
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Delegate one trending-post query to the current SQL discovery backend.
     * Params:
     * - query: normalized trending-post query
     * Returns:
     * - PageResponse<Post>: paginated trending post page
     * Throws: None
     */
    public PageResponse<Post> getTrendingPosts(TrendingPostsQuery query) {
        return sqlPostSearchService.getTrendingPosts(query);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Resolve one post page from AI-returned candidate ids while preserving AI order unless explicit SQL sort is requested.
     * Params:
     * - query: normalized search query
     * - postIds: AI-returned ordered post ids
     * Returns:
     * - PageResponse<Post>: paginated matched post page
     * Throws: None
     */
    private PageResponse<Post> buildAiOrderedPage(PostSearchQuery query, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return emptyPage(query.page(), query.pageSize());
        }
        List<Post> posts = postMapper.selectPublishedPostsByIds(
                postIds,
                query.categoryId(),
                query.storeId(),
                query.tagIds(),
                query.priceMin(),
                query.priceMax(),
                query.sort(),
                !query.sortExplicitlySpecified()
        );
        PageResponse<Post> response = new PageResponse<>();
        response.setTotal((long) posts.size());
        response.setPage(query.page());
        response.setPageSize(query.pageSize());
        response.setItems(slicePageItems(posts, query.offset(), query.pageSize()));
        return response;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Slice one full matched result list into the requested page window after AI ordering and filtering are applied.
     * Params:
     * - posts: full matched post list
     * - offset: target row offset
     * - pageSize: target page size
     * Returns:
     * - List<Post>: current page items
     * Throws: None
     */
    private List<Post> slicePageItems(List<Post> posts, int offset, int pageSize) {
        if (posts == null || posts.isEmpty() || offset >= posts.size()) {
            return List.of();
        }
        int endIndex = Math.min(offset + pageSize, posts.size());
        return posts.subList(offset, endIndex);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Build one empty page response for degraded image-search scenarios.
     * Params:
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - PageResponse<Post>: empty paginated response
     * Throws: None
     */
    private PageResponse<Post> emptyPage(int page, int pageSize) {
        PageResponse<Post> response = new PageResponse<>();
        response.setTotal(0L);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(List.of());
        return response;
    }
}
