package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

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
