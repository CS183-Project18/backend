package com.storefinds.uniquefindsbackend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Shuying Liang
 * Date: 2026-05-22
 * Purpose: Data payload for build index response containing success status and count
 * Params: None
 * Returns: None
 * Throws: None
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildIndexData {
    
    @JsonProperty("success")
    private Boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("count")
    private Integer count;

    @JsonProperty("semantic_count")
    private Integer semanticCount;

    @JsonProperty("image_count")
    private Integer imageCount;

    @JsonProperty("failed_image_count")
    private Integer failedImageCount;
}
