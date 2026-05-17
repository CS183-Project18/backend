package com.storefinds.uniquefindsbackend.dto;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Carry one normalized published-post search query across parser, discovery, and SQL search layers.
 * Params: None
 * Returns: None
 * Throws: None
 */
public record PostSearchQuery(String keyword,
                              String keywordLike,
                              Long categoryId,
                              String sort,
                              int page,
                              int pageSize,
                              int offset) {
}
