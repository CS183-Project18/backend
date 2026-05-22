package com.storefinds.uniquefindsbackend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Carry one normalized published-post search query across parser, discovery, and SQL search layers.
 * Params: None
 * Returns: None
 * Throws: None
 */
public record PostSearchQuery(String keyword,
                              String keywordLike,
                              Long categoryId,
                              Long storeId,
                              List<Long> tagIds,
                              BigDecimal priceMin,
                              BigDecimal priceMax,
                              String sort,
                              boolean sortExplicitlySpecified,
                              int page,
                              int pageSize,
                              int offset) {
}
