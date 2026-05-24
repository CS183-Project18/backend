package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.AdminPostModerationResponse;
import com.storefinds.uniquefindsbackend.dto.ModerationActionRequest;
import com.storefinds.uniquefindsbackend.dto.ModerationLogResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;

import java.time.LocalDateTime;

public interface AdminModerationService {

    Result<PageResponse<ReportResponse>> getReports(String targetType, String status, int page, int pageSize);

    Result<PageResponse<ModerationLogResponse>> getModerationLogs(String targetType,
                                                                  Long targetId,
                                                                  Long moderatorId,
                                                                  String action,
                                                                  LocalDateTime startTime,
                                                                  LocalDateTime endTime,
                                                                  int page,
                                                                  int pageSize);

    Result<PageResponse<AdminPostModerationResponse>> getPendingPosts(int page, int pageSize);

    Result<Void> approvePost(Long adminUserId, Long postId);

    Result<Void> rejectPost(Long adminUserId, Long postId, ModerationActionRequest request);

    Result<Void> hidePost(Long adminUserId, Long postId, ModerationActionRequest request);

    Result<Void> hideComment(Long adminUserId, Long commentId, ModerationActionRequest request);

    Result<Void> deleteComment(Long adminUserId, Long commentId, ModerationActionRequest request);

    Result<Void> resolveReport(Long adminUserId, Long reportId, ModerationActionRequest request);

    Result<Void> rejectReport(Long adminUserId, Long reportId, ModerationActionRequest request);
}
