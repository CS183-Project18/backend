package com.storefinds.uniquefindsbackend.controller.admin;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsOverviewResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsTrendsResponse;
import com.storefinds.uniquefindsbackend.service.AdminAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose read-only admin analytics endpoints for overview, trends, and distributions.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject admin analytics service for dashboard reporting endpoints.
     * Params:
     * - adminAnalyticsService: admin analytics business service
     * Returns: None
     * Throws: None
     */
    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/overview")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query aggregated overview counters for the admin dashboard.
     * Params: None
     * Returns:
     * - Result<AnalyticsOverviewResponse>: overview counters
     * Throws: None
     */
    public Result<AnalyticsOverviewResponse> getOverview() {
        return adminAnalyticsService.getOverview();
    }

    @GetMapping("/trends")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query dated trend series for the requested admin analytics window.
     * Params:
     * - window: trend window option
     * Returns:
     * - Result<AnalyticsTrendsResponse>: grouped trend data
     * Throws: None
     */
    public Result<AnalyticsTrendsResponse> getTrends(@RequestParam(defaultValue = "weekly") String window) {
        return adminAnalyticsService.getTrends(window);
    }

    @GetMapping("/distribution")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query distribution and ranking datasets for admin analytics views.
     * Params: None
     * Returns:
     * - Result<AnalyticsDistributionResponse>: distribution response
     * Throws: None
     */
    public Result<AnalyticsDistributionResponse> getDistribution() {
        return adminAnalyticsService.getDistribution();
    }
}
