package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private Long total;
    private Integer page;
    private Integer pageSize;
    private List<T> items;
}
