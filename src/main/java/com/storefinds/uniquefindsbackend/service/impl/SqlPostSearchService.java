package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.PostSearchService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
/**
 * Author: Shuying Liang
 * Date: 2026-05-14
 * Purpose: Execute the current MySQL-backed post discovery queries.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class SqlPostSearchService implements PostSearchService {

    private final PostMapper postMapper;

    /**
     * Author: Shuying Liang
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
     * Author: Shuying Liang
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
        response.setMetadata(Map.of("searchSource", "SQL"));
        return response;
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-05-14
     * Purpose: Execute one trending-post query within the requested time window.
     * Params:
     * - query: normalized trending-post query
     * Returns:
     * - PageResponse<Post>: paginated trending post page
     * Throws: None
     */
    public PageResponse<Post> getTrendingPosts(TrendingPostsQuery query) {
        PageResponse<Post> requestedPage = buildTrendingPage(query.window(), query.window(), query.windowStart(), query.page(), query.pageSize(), query.offset(), false);
        if (requestedPage.getTotal() > 0 || !"daily".equals(query.window())) {
            return requestedPage;
        }

        PageResponse<Post> weeklyPage = buildTrendingPage(query.window(), "weekly", LocalDateTime.now().minusWeeks(1), query.page(), query.pageSize(), query.offset(), true);
        if (weeklyPage.getTotal() > 0) {
            return weeklyPage;
        }
        return buildTrendingPage(query.window(), "monthly", LocalDateTime.now().minusMonths(1), query.page(), query.pageSize(), query.offset(), true);
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-05-22
     * Purpose: Build one trending page and annotate the effective window used by demo-friendly fallback logic.
     * Params:
     * - requestedWindow: caller requested window value
     * - effectiveWindow: actual query window value
     * - windowStart: inclusive SQL window start
     * - page: requested page number
     * - pageSize: requested page size
     * - offset: requested SQL offset
     * - fallbackApplied: whether a wider window was used
     * Returns:
     * - PageResponse<Post>: trending post page with metadata
     * Throws: None
     */
    private PageResponse<Post> buildTrendingPage(String requestedWindow,
                                                 String effectiveWindow,
                                                 LocalDateTime windowStart,
                                                 int page,
                                                 int pageSize,
                                                 int offset,
                                                 boolean fallbackApplied) {
        PageResponse<Post> response = new PageResponse<>();
        response.setTotal(postMapper.countTrendingPosts(windowStart));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(postMapper.selectTrendingPosts(windowStart, offset, pageSize));
        response.setMetadata(Map.of(
                "requestedWindow", requestedWindow,
                "effectiveWindow", effectiveWindow,
                "fallbackApplied", fallbackApplied
        ));
        return response;
    }
}
