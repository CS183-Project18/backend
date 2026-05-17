package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Represent one persisted notification record and joined actor username data.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class Notification {
    private Long id;
    private Long recipientUserId;
    private Long actorUserId;
    private String eventType;
    private String targetType;
    private Long targetId;
    private Long postId;
    private String metadata;
    private Integer isRead;
    private LocalDateTime createdAt;
    private String actorUsername;
}
