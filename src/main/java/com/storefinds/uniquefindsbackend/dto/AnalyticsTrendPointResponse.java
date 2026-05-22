package com.storefinds.uniquefindsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer one dated analytics trend point for chart-oriented admin responses.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AnalyticsTrendPointResponse {
    private String date;
    private long count;
}
