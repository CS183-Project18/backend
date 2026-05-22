package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Represent one tag dictionary record and optional post binding helper field for response assembly.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class Tag {
    private Long id;
    private Long postId;
    private String name;
    private BigDecimal heatScore;
    private LocalDateTime createdAt;
}
