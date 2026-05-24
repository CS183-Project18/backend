package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Author: Enqi Guo
 * Date: 2026-05-22
 * Purpose: Transfer admin moderation audit log rows with moderator and target display context.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class ModerationLogResponse {
    private Long id;
    private String targetType;
    private Long targetId;
    private Long moderatorId;
    private String moderatorUsername;
    private String action;
    private String reason;
    private String targetSummary;
    private LocalDateTime createdAt;
}
