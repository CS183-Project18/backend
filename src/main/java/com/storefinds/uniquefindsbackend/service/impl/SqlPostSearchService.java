package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.PostSearchService;
import org.springframework.stereotype.Service;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Execute the current MySQL-backed post discovery queries.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class SqlPostSearchService implements PostSearchService {

    private final PostMapper postMapper;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Inject the post mapper dependency used by MySQL-backed discovery queries.
     * Params:
     * - postMapper: post data access mapper
     * Returns: None
     * Throws: None
     */
    public SqlPostSearchService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Execute one published-post search query with structured filters and pagination.
     * Params:
     * - query: normalized published-post search query
     * Returns:
     * - PageResponse<Post>: paginated matched post page
     * Throws: None
     */
    public PageResponse<Post> searchPublishedPosts(PostSearchQuery query) {
        PageResponse<Post> response = new PageResponse<>();
        response.setTotal(postMapper.countSearchPublishedPosts(
                query.keyword(),
                query.keywordLike(),
                query.categoryId(),
                query.storeId(),
                query.tagIds(),
                query.priceMin(),
                query.priceMax()
        ));
        response.setPage(query.page());
        response.setPageSize(query.pageSize());
        response.setItems(postMapper.searchPublishedPosts(
                query.keyword(),
                query.keywordLike(),
                query.categoryId(),
                query.storeId(),
                query.tagIds(),
                query.priceMin(),
                query.priceMax(),
                query.sort(),
                query.offset(),
                query.pageSize()
        ));
        return response;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Execute one trending-post query within the requested time window.
     * Params:
     * - query: normalized trending-post query
     * Returns:
     * - PageResponse<Post>: paginated trending post page
     * Throws: None
     */
    public PageResponse<Post> getTrendingPosts(TrendingPostsQuery query) {
        PageResponse<Post> response = new PageResponse<>();
        response.setTotal(postMapper.countTrendingPosts(query.windowStart()));
        response.setPage(query.page());
        response.setPageSize(query.pageSize());
        response.setItems(postMapper.selectTrendingPosts(query.windowStart(), query.offset(), query.pageSize()));
        return response;
    }
}
