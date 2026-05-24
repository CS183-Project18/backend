package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
/**
 * Author: Enqi Guo
 * Date: 2026-05-18
 * Purpose: Transfer aggregated admin overview counters for non-AI analytics endpoints.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AnalyticsOverviewResponse {
    private long activeUserCount;
    private long postCreateCount;
    private long publishedPostCount;
    private long visibleCommentCount;
    private long favoriteCount;
    private long totalReportCount;
    private long pendingReportCount;
    private long searchRequestCount;
    private long shareCount;
    private long interactionEventCount;
    private Long averageReportResolutionHours;
    private long approvedModerationCount;
    private long rejectedModerationCount;
    private long hiddenModerationCount;
}
