package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Return stored-image metadata after the local upload module persists one file.
 */
@Data
public class ImageUploadResponse {
    private String url;
    private String thumbnailUrl;
    private String fileName;
    private String contentType;
    private Long size;
    private Integer width;
    private Integer height;
}
