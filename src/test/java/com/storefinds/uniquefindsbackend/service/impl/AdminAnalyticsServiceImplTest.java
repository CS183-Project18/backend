package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.ModerationActionType;
import com.storefinds.uniquefindsbackend.mapper.AnalyticsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Validate admin analytics overview aggregation behavior.
 */
@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceImplTest {

    @Mock
    private AnalyticsMapper analyticsMapper;

    @InjectMocks
    private AdminAnalyticsServiceImpl adminAnalyticsService;

    @Test
    void overviewIncludesV2ProductMetrics() {
        when(analyticsMapper.countCreatedPosts()).thenReturn(10L);
        when(analyticsMapper.countPublishedPosts()).thenReturn(7L);
        when(analyticsMapper.countInteractionEventsByType(InteractionEventType.SEARCH_REQUEST)).thenReturn(3L);
        when(analyticsMapper.countInteractionEventsByType(InteractionEventType.SHARE_LINK_CREATE)).thenReturn(2L);
        when(analyticsMapper.countInteractionEvents()).thenReturn(20L);
        when(analyticsMapper.averageReportResolutionHours()).thenReturn(5L);
        when(analyticsMapper.countModerationLogsByAction(ModerationActionType.APPROVE)).thenReturn(4L);
        when(analyticsMapper.countModerationLogsByAction(ModerationActionType.REJECT)).thenReturn(1L);
        when(analyticsMapper.countModerationLogsByAction(ModerationActionType.HIDE)).thenReturn(2L);

        var response = adminAnalyticsService.getOverview().data();

        assertEquals(10L, response.getPostCreateCount());
        assertEquals(7L, response.getPublishedPostCount());
        assertEquals(3L, response.getSearchRequestCount());
        assertEquals(2L, response.getShareCount());
        assertEquals(20L, response.getInteractionEventCount());
        assertEquals(5L, response.getAverageReportResolutionHours());
        assertEquals(4L, response.getApprovedModerationCount());
        assertEquals(1L, response.getRejectedModerationCount());
        assertEquals(2L, response.getHiddenModerationCount());
    }
}
