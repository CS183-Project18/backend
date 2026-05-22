package com.storefinds.uniquefindsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer one distribution bucket item for ranking and analytics endpoints.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AnalyticsDistributionItemResponse {
    private Long id;
    private String name;
    private long count;
}
