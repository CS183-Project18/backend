package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Carry admin update-store payload for structured store maintenance flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class UpdateStoreRequest {
    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must be at most 150 characters")
    private String name;

    @Size(max = 150, message = "branchName must be at most 150 characters")
    private String branchName;

    @Size(max = 80, message = "city must be at most 80 characters")
    private String city;

    @Size(max = 80, message = "district must be at most 80 characters")
    private String district;

    @Size(max = 255, message = "address must be at most 255 characters")
    private String address;

    @DecimalMin(value = "-90.0000000", message = "latitude is invalid")
    @DecimalMax(value = "90.0000000", message = "latitude is invalid")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0000000", message = "longitude is invalid")
    @DecimalMax(value = "180.0000000", message = "longitude is invalid")
    private BigDecimal longitude;

    @Size(max = 30, message = "phone must be at most 30 characters")
    private String phone;

    @Size(max = 120, message = "businessHours must be at most 120 characters")
    private String businessHours;
}
