package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Carry admin create-tag payload for tag dictionary maintenance.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CreateTagRequest {
    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;
}
