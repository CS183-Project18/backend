package com.storefinds.uniquefindsbackend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Request DTO for building AI search text index
 * Params: None
 * Returns: None
 * Throws: None
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildIndexRequest {
    
    @JsonProperty("posts")
    private List<IndexPostData> posts;
}
