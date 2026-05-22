package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CategoryResponse;
import com.storefinds.uniquefindsbackend.dto.CategorySummaryResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCategoryRequest;
import com.storefinds.uniquefindsbackend.dto.UpdateCategoryRequest;

import java.util.List;
import java.util.Map;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Define category taxonomy query, validation, and admin maintenance capabilities.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface CategoryService {

    Result<List<CategoryResponse>> getCategories(boolean activeOnly);

    Result<List<CategoryResponse>> getCategoryTree(boolean activeOnly);

    Result<CategoryResponse> createCategory(CreateCategoryRequest request);

    Result<CategoryResponse> updateCategory(Long categoryId, UpdateCategoryRequest request);

    Result<Void> updateCategoryActive(Long categoryId, boolean active);

    Map<Long, CategorySummaryResponse> getCategorySummaryMap(List<Long> categoryIds);

    void validateCategorySelectable(Long categoryId);
}
