package com.storefinds.uniquefindsbackend.dto;

import java.time.LocalDateTime;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Carry one normalized trending-post query across parser and discovery layers.
 * Params: None
 * Returns: None
 * Throws: None
 */
public record TrendingPostsQuery(String window,
                                 LocalDateTime windowStart,
                                 int page,
                                 int pageSize,
                                 int offset) {
}
