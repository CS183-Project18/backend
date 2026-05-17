package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
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
    private Boolean read;
    private LocalDateTime createdAt;
}
