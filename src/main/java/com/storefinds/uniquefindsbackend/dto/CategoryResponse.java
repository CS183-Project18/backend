package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Transfer category detail data including tree children for frontend taxonomy rendering.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CategoryResponse {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryResponse> children;
}
