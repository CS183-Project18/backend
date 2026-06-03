package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateReportRequest;
import com.storefinds.uniquefindsbackend.dto.ReportResponse;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Define the reporting workflow contract for post and comment abuse reports.
 */
public interface ReportService {

    Result<ReportResponse> reportPost(Long userId, Long postId, CreateReportRequest request);

    Result<ReportResponse> reportComment(Long userId, Long commentId, CreateReportRequest request);
}
