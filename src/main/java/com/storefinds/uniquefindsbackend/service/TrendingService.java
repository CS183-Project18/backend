package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionItemResponse;

import java.util.List;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Define non-post trending query capabilities for discovery pages.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface TrendingService {

    Result<List<AnalyticsDistributionItemResponse>> getTrendingCategories(int limit);

    Result<List<AnalyticsDistributionItemResponse>> getTrendingTags(int limit);

    Result<List<AnalyticsDistributionItemResponse>> getTrendingStores(int limit);
}
