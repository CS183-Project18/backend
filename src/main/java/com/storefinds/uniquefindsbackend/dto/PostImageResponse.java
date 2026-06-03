package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Return normalized post-image metadata for feed, detail, and moderation views.
 */
@Data
public class PostImageResponse {
    private Long id;
    private String imageUrl;
    private String imageKey;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private String mimeType;
    private Integer sortOrder;
    private Integer isCover;
}
