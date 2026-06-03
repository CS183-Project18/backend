package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Capture one image item attached to a post creation or post update request.
 */
@Data
public class PostImageRequest {

    @NotBlank(message = "imageUrl is required")
    @Size(max = 500, message = "imageUrl must be at most 500 characters")
    private String imageUrl;

    @Size(max = 255, message = "imageKey must be at most 255 characters")
    private String imageKey;

    @Size(max = 500, message = "thumbnailUrl must be at most 500 characters")
    private String thumbnailUrl;

    private Integer width;

    private Integer height;

    private Long fileSize;

    @Size(max = 80, message = "mimeType must be at most 80 characters")
    private String mimeType;
}
