package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminPostModerationResponse {
    private Long id;
    private Long userId;
    private String authorUsername;
    private Long storeId;
    private Long categoryId;
    private String title;
    private String description;
    private List<PostImageResponse> images;
    private String status;
    private String moderationReason;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
