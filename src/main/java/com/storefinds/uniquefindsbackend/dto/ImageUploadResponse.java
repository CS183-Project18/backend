package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

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
