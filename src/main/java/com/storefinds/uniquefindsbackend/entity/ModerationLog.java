package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Represent one persistent audit row created by the moderation governance module.
 */
@Data
public class ModerationLog {
    private Long id;
    private String targetType;
    private Long targetId;
    private Long moderatorId;
    private String moderatorUsername;
    private String action;
    private String reason;
    private LocalDateTime createdAt;
}
