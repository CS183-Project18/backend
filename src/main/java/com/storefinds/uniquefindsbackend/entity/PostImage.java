package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Represent one persisted image record belonging to a post in the media module.
 */
@Data
public class PostImage {
    private Long id;
    private Long postId;
    private String imageUrl;
    private String imageKey;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private String mimeType;
    private Integer sortOrder;
    private Integer isCover;
    private LocalDateTime createdAt;
}
