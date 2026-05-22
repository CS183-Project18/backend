package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionItemResponse;
import com.storefinds.uniquefindsbackend.mapper.AnalyticsMapper;
import com.storefinds.uniquefindsbackend.service.TrendingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Implement non-post trending queries for categories, tags, and stores.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class TrendingServiceImpl implements TrendingService {

    private final AnalyticsMapper analyticsMapper;

    public TrendingServiceImpl(AnalyticsMapper analyticsMapper) {
        this.analyticsMapper = analyticsMapper;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query top category rankings for discovery pages.
     * Params:
     * - limit: maximum returned category count
     * Returns:
     * - Result<List<AnalyticsDistributionItemResponse>>: ranked category list
     * Throws: None
     */
    public Result<List<AnalyticsDistributionItemResponse>> getTrendingCategories(int limit) {
        return Result.success(toItems(analyticsMapper.topCategories(limit)));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query top tag rankings for discovery pages.
     * Params:
     * - limit: maximum returned tag count
     * Returns:
     * - Result<List<AnalyticsDistributionItemResponse>>: ranked tag list
     * Throws: None
     */
    public Result<List<AnalyticsDistributionItemResponse>> getTrendingTags(int limit) {
        return Result.success(toItems(analyticsMapper.topTags(limit)));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query top store rankings for discovery pages.
     * Params:
     * - limit: maximum returned store count
     * Returns:
     * - Result<List<AnalyticsDistributionItemResponse>>: ranked store list
     * Throws: None
     */
    public Result<List<AnalyticsDistributionItemResponse>> getTrendingStores(int limit) {
        return Result.success(toItems(analyticsMapper.topStores(limit)));
    }

    private List<AnalyticsDistributionItemResponse> toItems(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> new AnalyticsDistributionItemResponse(
                        row.get("id") == null ? null : ((Number) row.get("id")).longValue(),
                        String.valueOf(row.get("name")),
                        ((Number) row.get("count")).longValue()
                ))
                .toList();
    }
}
