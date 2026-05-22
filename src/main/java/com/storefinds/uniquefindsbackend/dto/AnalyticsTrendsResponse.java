package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.util.List;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer grouped analytics trend series for admin dashboard consumption.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AnalyticsTrendsResponse {
    private List<AnalyticsTrendPointResponse> postCreates;
    private List<AnalyticsTrendPointResponse> commentCreates;
    private List<AnalyticsTrendPointResponse> favorites;
    private List<AnalyticsTrendPointResponse> reports;
    private List<AnalyticsTrendPointResponse> reportHandled;
}
