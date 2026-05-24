package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
/**
 * Author: Enqi Guo
 * Date: 2026-05-22
 * Purpose: Transfer lightweight data consistency counters for admin maintenance checks.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class DataConsistencyResponse {
    private Long orphanPostImageCount;
    private Long orphanPostTagCount;
    private Long missingActiveCategoryPostCount;
    private Long missingActiveStorePostCount;
    private Long publishedPostWithoutImageCount;
}
