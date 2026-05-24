package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.ModerationActionType;
import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionItemResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsOverviewResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsTrendPointResponse;
import com.storefinds.uniquefindsbackend.dto.AnalyticsTrendsResponse;
import com.storefinds.uniquefindsbackend.dto.DataConsistencyResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.AnalyticsMapper;
import com.storefinds.uniquefindsbackend.service.AdminAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Enqi Guo
 * Date: 2026-05-18
 * Purpose: Implement read-only analytics aggregation for admin overview, trends, and rankings.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final AnalyticsMapper analyticsMapper;

    public AdminAnalyticsServiceImpl(AnalyticsMapper analyticsMapper) {
        this.analyticsMapper = analyticsMapper;
    }

    @Override
    /**
     * Author: Enqi Guo
     * Date: 2026-05-18
     * Purpose: Aggregate top-level overview counters for admin analytics dashboards.
     * Params: None
     * Returns:
     * - Result<AnalyticsOverviewResponse>: overview counters
     * Throws: None
     */
    public Result<AnalyticsOverviewResponse> getOverview() {
        AnalyticsOverviewResponse response = new AnalyticsOverviewResponse();
        response.setActiveUserCount(analyticsMapper.countActiveUsers());
        response.setPostCreateCount(analyticsMapper.countCreatedPosts());
        response.setPublishedPostCount(analyticsMapper.countPublishedPosts());
        response.setVisibleCommentCount(analyticsMapper.countVisibleComments());
        response.setFavoriteCount(analyticsMapper.countFavorites());
        response.setTotalReportCount(analyticsMapper.countReports());
        response.setPendingReportCount(analyticsMapper.countPendingReports());
        response.setSearchRequestCount(analyticsMapper.countInteractionEventsByType(InteractionEventType.SEARCH_REQUEST));
        response.setShareCount(analyticsMapper.countInteractionEventsByType(InteractionEventType.SHARE_LINK_CREATE));
        response.setInteractionEventCount(analyticsMapper.countInteractionEvents());
        response.setAverageReportResolutionHours(analyticsMapper.averageReportResolutionHours());
        response.setApprovedModerationCount(analyticsMapper.countModerationLogsByAction(ModerationActionType.APPROVE));
        response.setRejectedModerationCount(analyticsMapper.countModerationLogsByAction(ModerationActionType.REJECT));
        response.setHiddenModerationCount(analyticsMapper.countModerationLogsByAction(ModerationActionType.HIDE));
        return Result.success(response);
    }

    @Override
    /**
     * Author: Enqi Guo
     * Date: 2026-05-18
     * Purpose: Aggregate dated activity trends for posts, comments, favorites, and reports.
     * Params:
     * - window: daily, weekly, or monthly trend window
     * Returns:
     * - Result<AnalyticsTrendsResponse>: grouped trend series
     * Throws:
     * - BusinessException: when the requested window is invalid
     */
    public Result<AnalyticsTrendsResponse> getTrends(String window) {
        LocalDateTime startTime = resolveWindowStart(window);
        AnalyticsTrendsResponse response = new AnalyticsTrendsResponse();
        response.setPostCreates(toTrendPoints(analyticsMapper.countPostCreatesByDay(startTime)));
        response.setCommentCreates(toTrendPoints(analyticsMapper.countCommentCreatesByDay(startTime)));
        response.setFavorites(toTrendPoints(analyticsMapper.countFavoritesByDay(startTime)));
        response.setReports(toTrendPoints(analyticsMapper.countReportsByDay(startTime)));
        response.setReportHandled(toTrendPoints(analyticsMapper.countHandledReportsByDay(startTime)));
        return Result.success(response);
    }

    @Override
    /**
     * Author: Enqi Guo
     * Date: 2026-05-18
     * Purpose: Aggregate distribution-style analytics for report reasons and top structured content dimensions.
     * Params: None
     * Returns:
     * - Result<AnalyticsDistributionResponse>: grouped distribution data
     * Throws: None
     */
    public Result<AnalyticsDistributionResponse> getDistribution() {
        AnalyticsDistributionResponse response = new AnalyticsDistributionResponse();
        response.setReportReasons(toDistributionItems(analyticsMapper.countReportsByReason()));
        response.setTopCategories(toDistributionItems(analyticsMapper.topCategories(10)));
        response.setTopTags(toDistributionItems(analyticsMapper.topTags(10)));
        response.setTopStores(toDistributionItems(analyticsMapper.topStores(10)));
        response.setAvgReportResolutionHours(analyticsMapper.averageReportResolutionHours());
        return Result.success(response);
    }

    @Override
    /**
     * Author: Enqi Guo
     * Date: 2026-05-22
     * Purpose: Aggregate lightweight consistency counters for admin maintenance checks.
     * Params: None
     * Returns:
     * - Result<DataConsistencyResponse>: consistency counters
     * Throws: None
     */
    public Result<DataConsistencyResponse> getDataConsistency() {
        DataConsistencyResponse response = new DataConsistencyResponse();
        response.setOrphanPostImageCount(analyticsMapper.countOrphanPostImages());
        response.setOrphanPostTagCount(analyticsMapper.countOrphanPostTags());
        response.setMissingActiveCategoryPostCount(analyticsMapper.countPublishedPostsMissingActiveCategory());
        response.setMissingActiveStorePostCount(analyticsMapper.countPublishedPostsMissingActiveStore());
        response.setPublishedPostWithoutImageCount(analyticsMapper.countPublishedPostsWithoutImages());
        return Result.success(response);
    }

    private LocalDateTime resolveWindowStart(String window) {
        String normalized = window == null ? "weekly" : window.trim().toLowerCase();
        return switch (normalized) {
            case "daily" -> LocalDateTime.now().minusDays(1);
            case "weekly" -> LocalDateTime.now().minusWeeks(1);
            case "monthly" -> LocalDateTime.now().minusMonths(1);
            default -> throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "window must be one of: daily, weekly, monthly");
        };
    }

    private List<AnalyticsTrendPointResponse> toTrendPoints(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> new AnalyticsTrendPointResponse(String.valueOf(row.get("date")), ((Number) row.get("count")).longValue()))
                .toList();
    }

    private List<AnalyticsDistributionItemResponse> toDistributionItems(List<Map<String, Object>> rows) {
        return rows.stream()
                .map(row -> new AnalyticsDistributionItemResponse(
                        row.get("id") instanceof Number number ? number.longValue() : null,
                        String.valueOf(row.get("name")),
                        ((Number) row.get("count")).longValue()
                ))
                .toList();
    }
}
