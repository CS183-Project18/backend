package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionItemResponse;
import com.storefinds.uniquefindsbackend.service.TrendingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/discovery")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose public non-post discovery ranking endpoints for categories, tags, and stores.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class DiscoveryController {

    private final TrendingService trendingService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject trending service for public discovery ranking endpoints.
     * Params:
     * - trendingService: discovery ranking business service
     * Returns: None
     * Throws: None
     */
    public DiscoveryController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @GetMapping("/trending/categories")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query trending categories for public discovery entry points.
     * Params:
     * - limit: maximum returned category count
     * Returns:
     * - Result<List<AnalyticsDistributionItemResponse>>: ranked categories
     * Throws: None
     */
    public Result<List<AnalyticsDistributionItemResponse>> getTrendingCategories(@RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        return trendingService.getTrendingCategories(limit);
    }

    @GetMapping("/trending/tags")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query trending tags for public discovery entry points.
     * Params:
     * - limit: maximum returned tag count
     * Returns:
     * - Result<List<AnalyticsDistributionItemResponse>>: ranked tags
     * Throws: None
     */
    public Result<List<AnalyticsDistributionItemResponse>> getTrendingTags(@RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        return trendingService.getTrendingTags(limit);
    }

    @GetMapping("/trending/stores")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query trending stores for public discovery entry points.
     * Params:
     * - limit: maximum returned store count
     * Returns:
     * - Result<List<AnalyticsDistributionItemResponse>>: ranked stores
     * Throws: None
     */
    public Result<List<AnalyticsDistributionItemResponse>> getTrendingStores(@RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        return trendingService.getTrendingStores(limit);
    }
}
