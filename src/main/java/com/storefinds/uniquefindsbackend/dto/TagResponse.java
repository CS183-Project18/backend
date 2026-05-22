package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer tag data for post detail, tag listing, and discovery-related responses.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class TagResponse {
    private Long id;
    private String name;
    private BigDecimal heatScore;
    private LocalDateTime createdAt;
}
