package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Return normalized report details for reporter confirmation and admin moderation review.
 */
@Data
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterUsername;
    private String targetType;
    private Long targetId;
    private String reasonType;
    private String reasonDetail;
    private String status;
    private String resolutionAction;
    private String resolutionNote;
    private String targetStatus;
    private String targetSummary;
    private Long handledBy;
    private String handledByUsername;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
