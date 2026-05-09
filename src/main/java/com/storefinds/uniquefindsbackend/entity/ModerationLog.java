package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModerationLog {
    private Long id;
    private String targetType;
    private Long targetId;
    private Long moderatorId;
    private String action;
    private String reason;
    private LocalDateTime createdAt;
}
