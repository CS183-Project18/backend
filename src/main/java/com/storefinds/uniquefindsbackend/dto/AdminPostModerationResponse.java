package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPostModerationResponse {
    private Long id;
    private Long userId;
    private String authorUsername;
    private Long storeId;
    private Long categoryId;
    private String title;
    private String description;
    private String status;
    private String moderationReason;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
