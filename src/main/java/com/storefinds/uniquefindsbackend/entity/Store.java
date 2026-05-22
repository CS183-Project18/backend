package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Represent one store record used by structured post metadata and discovery filters.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class Store {
    private Long id;
    private String name;
    private String branchName;
    private String city;
    private String district;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String businessHours;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
