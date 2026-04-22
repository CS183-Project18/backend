package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

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
