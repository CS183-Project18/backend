package com.storefinds.uniquefindsbackend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Response DTO for AI search service health check
 * Params: None
 * Returns: None
 * Throws: None
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("data")
    private HealthData data;
    
    @JsonProperty("message")
    private String message;
}
