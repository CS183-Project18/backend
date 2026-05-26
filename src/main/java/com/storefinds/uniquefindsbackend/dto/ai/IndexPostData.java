package com.storefinds.uniquefindsbackend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: Shuying Liang
 * Date: 2026-05-22
 * Purpose: Post data DTO for AI search index building
 * Params: None
 * Returns: None
 * Throws: None
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexPostData {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("store_name")
    private String storeName;

    @JsonProperty("location_text")
    private String locationText;
}
