package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
/**
 * Author: Enqi Guo
 * Date: 2026-05-10
 * Purpose: Transfer notification data required by the notification list API.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class NotificationResponse {
    private Long id;
    private String eventType;
    private Long actorUserId;
    private String actorUsername;
    private String targetType;
    private Long targetId;
    private Long postId;
    private String message;
    private String targetSummary;
    private Map<String, Object> metadata;
    private Boolean read;
    private LocalDateTime createdAt;
}
