package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Carry admin update-category payload for taxonomy maintenance endpoints.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class UpdateCategoryRequest {
    private Long parentId;

    @NotBlank(message = "name is required")
    @Size(max = 80, message = "name must be at most 80 characters")
    private String name;

    @Min(value = 0, message = "sortOrder must be greater than or equal to 0")
    private Integer sortOrder;

    @Min(value = 1, message = "level must be between 1 and 3")
    @Max(value = 3, message = "level must be between 1 and 3")
    private Integer level;
}
