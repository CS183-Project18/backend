package com.storefinds.uniquefindsbackend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: Shuying Liang
 * Date: 2026-05-22
 * Purpose: Data payload for AI search response containing post IDs and cache status
 * Params: None
 * Returns: None
 * Throws: None
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AISearchData {
    
    @JsonProperty("post_ids")
    private List<Long> postIds;
    
    @JsonProperty("cached")
    private Boolean cached;
}
