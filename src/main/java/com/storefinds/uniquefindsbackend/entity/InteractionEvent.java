package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-15
 * Purpose: Represent one persisted lightweight interaction event used for analytics-ready activity tracking.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class InteractionEvent {
    private Long id;
    private String eventType;
    private Long userId;
    private Long postId;
    private Long commentId;
    private String targetType;
    private Long targetId;
    private BigDecimal eventValue;
    private String metadata;
    private LocalDateTime eventTime;
}
