package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer lightweight category summary data embedded in post responses.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CategorySummaryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
}
