package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Represent one stored abuse report handled by the moderation and governance module.
 */
@Data
public class Report {
    private Long id;
    private Long reporterId;
    private String targetType;
    private Long targetId;
    private String reasonType;
    private String reasonDetail;
    private String status;
    private String resolutionAction;
    private String resolutionNote;
    private Long handledBy;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    private String reporterUsername;
    private String handledByUsername;
}
