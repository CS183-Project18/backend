package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

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
