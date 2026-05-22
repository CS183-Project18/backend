package com.storefinds.uniquefindsbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Provide read-only aggregate queries for admin analytics, rankings, and governance statistics.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface AnalyticsMapper {

    long countActiveUsers();

    long countPublishedPosts();

    long countVisibleComments();

    long countFavorites();

    long countReports();

    long countPendingReports();

    Long averageReportResolutionHours();

    List<Map<String, Object>> countPostCreatesByDay(@Param("startTime") LocalDateTime startTime);

    List<Map<String, Object>> countCommentCreatesByDay(@Param("startTime") LocalDateTime startTime);

    List<Map<String, Object>> countFavoritesByDay(@Param("startTime") LocalDateTime startTime);

    List<Map<String, Object>> countReportsByDay(@Param("startTime") LocalDateTime startTime);

    List<Map<String, Object>> countHandledReportsByDay(@Param("startTime") LocalDateTime startTime);

    List<Map<String, Object>> countReportsByReason();

    List<Map<String, Object>> topCategories(@Param("limit") int limit);

    List<Map<String, Object>> topTags(@Param("limit") int limit);

    List<Map<String, Object>> topStores(@Param("limit") int limit);
}
