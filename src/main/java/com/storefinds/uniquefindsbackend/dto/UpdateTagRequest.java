package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Carry admin update-tag payload including optional heat score adjustments.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class UpdateTagRequest {
    @NotBlank(message = "name is required")
    @Size(max = 60, message = "name must be at most 60 characters")
    private String name;

    @DecimalMin(value = "0.00", message = "heatScore must be greater than or equal to 0")
    private BigDecimal heatScore;
}
