package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.PostSortOption;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

@Component
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Normalize and validate discovery query inputs before SQL execution.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class SearchQueryParser {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Normalize published-post search inputs including structured filters and price range constraints.
     * Params:
     * - keyword: optional search keyword
     * - categoryId: optional category id
     * - storeId: optional store id
     * - tagIds: optional tag id list
     * - priceMin: optional minimum price filter
     * - priceMax: optional maximum price filter
     * - sort: optional sort option
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - PostSearchQuery: normalized search query object
     * Throws:
     * - BusinessException: when price range or sort option is invalid
     */
    public PostSearchQuery parsePostSearchQuery(String keyword,
                                                Long categoryId,
                                                Long storeId,
                                                List<Long> tagIds,
                                                BigDecimal priceMin,
                                                BigDecimal priceMax,
                                                String sort,
                                                int page,
                                                int pageSize) {
        String normalizedKeyword = normalizeOptionalText(keyword);
        validatePriceRange(priceMin, priceMax);
        return new PostSearchQuery(
                normalizedKeyword,
                normalizedKeyword == null ? null : "%" + normalizedKeyword + "%",
                categoryId,
                storeId,
                normalizeTagIds(tagIds),
                priceMin,
                priceMax,
                normalizeSort(sort),
                isSortExplicitlySpecified(sort),
                page,
                pageSize,
                toOffset(page, pageSize)
        );
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Normalize trending-post query inputs and resolve the corresponding time window start.
     * Params:
     * - window: requested trending window
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - TrendingPostsQuery: normalized trending query object
     * Throws:
     * - BusinessException: when the requested window is invalid
     */
    public TrendingPostsQuery parseTrendingPostsQuery(String window, int page, int pageSize) {
        String normalizedWindow = normalizeOptionalText(window);
        if (normalizedWindow == null || "daily".equalsIgnoreCase(normalizedWindow)) {
            return new TrendingPostsQuery("daily", LocalDateTime.now().minusDays(1), page, pageSize, toOffset(page, pageSize));
        }
        if ("weekly".equalsIgnoreCase(normalizedWindow)) {
            return new TrendingPostsQuery("weekly", LocalDateTime.now().minusWeeks(1), page, pageSize, toOffset(page, pageSize));
        }
        if ("monthly".equalsIgnoreCase(normalizedWindow)) {
            return new TrendingPostsQuery("monthly", LocalDateTime.now().minusMonths(1), page, pageSize, toOffset(page, pageSize));
        }
        throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "window must be one of: daily, weekly, monthly");
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Normalize one optional text value by trimming whitespace and collapsing blank to null.
     * Params:
     * - value: raw text value
     * Returns:
     * - String: normalized text or null
     * Throws: None
     */
    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Normalize and validate supported search sort options for discovery queries.
     * Params:
     * - sort: raw sort value
     * Returns:
     * - String: normalized sort option
     * Throws:
     * - BusinessException: when sort option is unsupported
     */
    private String normalizeSort(String sort) {
        String normalized = normalizeOptionalText(sort);
        if (normalized == null) {
            return PostSortOption.LATEST;
        }
        if (PostSortOption.LATEST.equalsIgnoreCase(normalized)) {
            return PostSortOption.LATEST;
        }
        if (PostSortOption.HOT.equalsIgnoreCase(normalized)) {
            return PostSortOption.HOT;
        }
        if (PostSortOption.FAVORITES.equalsIgnoreCase(normalized)) {
            return PostSortOption.FAVORITES;
        }
        if (PostSortOption.COMMENTS.equalsIgnoreCase(normalized)) {
            return PostSortOption.COMMENTS;
        }
        throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "sort must be one of: latest, hot, favorites, comments");
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Determine whether the caller explicitly supplied one supported sort option.
     * Params:
     * - sort: raw sort value
     * Returns:
     * - boolean: true when the caller supplied sort input, otherwise false
     * Throws: None
     */
    private boolean isSortExplicitlySpecified(String sort) {
        return normalizeOptionalText(sort) != null;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Normalize tag filter ids by removing nulls, invalid values, and duplicates.
     * Params:
     * - tagIds: raw tag id list
     * Returns:
     * - List<Long>: normalized tag id list
     * Throws: None
     */
    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<Long> normalized = tagIds.stream()
                .filter(tagId -> tagId != null && tagId > 0)
                .distinct()
                .toList();
        if (normalized.size() != tagIds.size()) {
            return List.copyOf(new LinkedHashSet<>(normalized));
        }
        return normalized;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Validate that the optional price range is logically ordered.
     * Params:
     * - priceMin: optional minimum price
     * - priceMax: optional maximum price
     * Returns: None
     * Throws:
     * - BusinessException: when priceMin is greater than priceMax
     */
    private void validatePriceRange(BigDecimal priceMin, BigDecimal priceMax) {
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "priceMin cannot be greater than priceMax");
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Convert one page request to SQL offset form.
     * Params:
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - int: SQL row offset
     * Throws: None
     */
    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }
}
