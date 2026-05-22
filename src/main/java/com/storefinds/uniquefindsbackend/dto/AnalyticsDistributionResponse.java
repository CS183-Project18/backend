package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.util.List;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer grouped analytics distribution data for reports, categories, tags, and stores.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AnalyticsDistributionResponse {
    private List<AnalyticsDistributionItemResponse> reportReasons;
    private List<AnalyticsDistributionItemResponse> topCategories;
    private List<AnalyticsDistributionItemResponse> topTags;
    private List<AnalyticsDistributionItemResponse> topStores;
    private Long avgReportResolutionHours;
}
