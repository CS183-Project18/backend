package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer lightweight store summary data embedded in post responses and discovery results.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class StoreSummaryResponse {
    private Long id;
    private String name;
    private String branchName;
    private String city;
    private String district;
    private String status;
}
