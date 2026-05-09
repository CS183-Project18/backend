package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
    private Long handledBy;
    private String handledByUsername;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
