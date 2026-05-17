package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
public class ImageUploadResponse {
    private String url;
    private String fileName;
    private String contentType;
    private Long size;
}
