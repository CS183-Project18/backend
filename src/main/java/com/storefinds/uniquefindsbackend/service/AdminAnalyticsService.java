package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsOverviewResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsTrendsResponse;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Define read-only admin analytics capabilities for overview, trends, and distributions.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface AdminAnalyticsService {

    Result<AnalyticsOverviewResponse> getOverview();

    Result<AnalyticsTrendsResponse> getTrends(String window);

    Result<AnalyticsDistributionResponse> getDistribution();
}
