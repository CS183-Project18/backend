package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.PostSortOption;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

    public PostSearchQuery parsePostSearchQuery(String keyword,
                                                Long categoryId,
                                                String sort,
                                                int page,
                                                int pageSize) {
        String normalizedKeyword = normalizeOptionalText(keyword);
        return new PostSearchQuery(
                normalizedKeyword,
                normalizedKeyword == null ? null : "%" + normalizedKeyword + "%",
                categoryId,
                normalizeSort(sort),
                page,
                pageSize,
                toOffset(page, pageSize)
        );
    }

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

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

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
        throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "sort must be one of: latest, hot");
    }

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }
}
