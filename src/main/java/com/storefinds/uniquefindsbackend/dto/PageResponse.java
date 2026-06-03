package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Provide a reusable paginated response envelope across list-style backend modules.
 */
@Data
public class PageResponse<T> {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<T> items;
    private Map<String, Object> metadata = Map.of();
}
