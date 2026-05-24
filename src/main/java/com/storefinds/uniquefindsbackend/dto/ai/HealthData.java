package com.storefinds.uniquefindsbackend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Shuying Liang
 * Date: 2026-05-22
 * Purpose: Data payload for health check response containing service status
 * Params: None
 * Returns: None
 * Throws: None
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthData {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("timestamp")
    private String timestamp;
}
