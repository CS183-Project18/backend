package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CategoryResponse;
import com.storefinds.uniquefindsbackend.dto.CategorySummaryResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCategoryRequest;
import com.storefinds.uniquefindsbackend.dto.UpdateCategoryRequest;
import com.storefinds.uniquefindsbackend.entity.Category;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CategoryMapper;
import com.storefinds.uniquefindsbackend.service.CategoryService;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Implement category taxonomy query, validation, tree assembly, and admin maintenance flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final IndexSyncService indexSyncService;

    public CategoryServiceImpl(CategoryMapper categoryMapper,
                               IndexSyncService indexSyncService) {
        this.categoryMapper = categoryMapper;
        this.indexSyncService = indexSyncService;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query flat category data for frontend filters or admin management lists.
     * Params:
     * - activeOnly: whether to keep only active categories
     * Returns:
     * - Result<List<CategoryResponse>>: flat category list
     * Throws: None
     */
    public Result<List<CategoryResponse>> getCategories(boolean activeOnly) {
        return Result.success((activeOnly ? categoryMapper.selectActive() : categoryMapper.selectAll())
                .stream()
                .map(category -> toResponse(category, List.of()))
                .toList());
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Build a category tree response for frontend taxonomy rendering.
     * Params:
     * - activeOnly: whether to keep only active categories
     * Returns:
     * - Result<List<CategoryResponse>>: tree-structured category list
     * Throws: None
     */
    public Result<List<CategoryResponse>> getCategoryTree(boolean activeOnly) {
        List<Category> categories = activeOnly ? categoryMapper.selectActive() : categoryMapper.selectAll();
        return Result.success(buildTree(categories));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Create one category node under the specified parent for admin taxonomy maintenance.
     * Params:
     * - request: create category payload
     * Returns:
     * - Result<CategoryResponse>: created category detail
     * Throws:
     * - BusinessException: when hierarchy or level settings are invalid
     */
    public Result<CategoryResponse> createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        applyCategoryRequest(category, request.getParentId(), request.getName(), request.getSortOrder(), request.getLevel(), true);
        categoryMapper.insert(category);
        return Result.success("category created", toResponse(categoryMapper.selectById(category.getId()), List.of()));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one category node including parent, display name, and sort metadata.
     * Params:
     * - categoryId: target category id
     * - request: update category payload
     * Returns:
     * - Result<CategoryResponse>: updated category detail
     * Throws:
     * - BusinessException: when target category or parent configuration is invalid
     */
    public Result<CategoryResponse> updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = requireCategory(categoryId);
        applyCategoryRequest(category, request.getParentId(), request.getName(), request.getSortOrder(), request.getLevel(), false);
        categoryMapper.updateById(category);
        indexSyncService.scheduleRebuild("category updated: categoryId=" + categoryId);
        return Result.success("category updated", toResponse(categoryMapper.selectById(categoryId), List.of()));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Enable or disable one category for future content selection and discovery filters.
     * Params:
     * - categoryId: target category id
     * - active: target active flag
     * Returns:
     * - Result<Void>: update result
     * Throws:
     * - BusinessException: when target category does not exist
     */
    public Result<Void> updateCategoryActive(Long categoryId, boolean active) {
        requireCategory(categoryId);
        categoryMapper.updateActiveById(categoryId, active ? 1 : 0);
        indexSyncService.scheduleRebuild("category active status updated: categoryId=" + categoryId);
        return Result.success("category status updated", null);
    }

    @Override
    public Map<Long, CategorySummaryResponse> getCategorySummaryMap(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, CategorySummaryResponse> summaryMap = new LinkedHashMap<>();
        for (Long categoryId : categoryIds) {
            if (categoryId == null || summaryMap.containsKey(categoryId)) {
                continue;
            }
            Category category = categoryMapper.selectById(categoryId);
            if (category != null) {
                CategorySummaryResponse summary = new CategorySummaryResponse();
                summary.setId(category.getId());
                summary.setName(category.getName());
                summary.setParentId(category.getParentId());
                summary.setLevel(category.getLevel());
                summaryMap.put(category.getId(), summary);
            }
        }
        return summaryMap;
    }

    @Override
    public void validateCategorySelectable(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        Category category = requireCategory(categoryId);
        if (category.getIsActive() == null || category.getIsActive() != 1) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "category is not active");
        }
    }

    private Category requireCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "category not found");
        }
        return category;
    }

    private void applyCategoryRequest(Category category,
                                      Long parentId,
                                      String name,
                                      Integer sortOrder,
                                      Integer level,
                                      boolean creating) {
        validateParentAssignment(category.getId(), parentId, creating);
        String normalizedName = normalizeRequiredText(name, "name is required");
        int resolvedSortOrder = sortOrder == null ? 0 : sortOrder;
        int resolvedLevel = resolveLevel(parentId, level);
        category.setParentId(parentId);
        category.setName(normalizedName);
        category.setSortOrder(resolvedSortOrder);
        category.setLevel(resolvedLevel);
        if (creating) {
            category.setIsActive(1);
        }
    }

    private int resolveLevel(Long parentId, Integer requestedLevel) {
        if (parentId == null) {
            if (requestedLevel != null && requestedLevel != 1) {
                throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "root category level must be 1");
            }
            return 1;
        }
        Category parent = requireCategory(parentId);
        if (parent.getIsActive() == null || parent.getIsActive() != 1) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "parent category is not active");
        }
        int level = parent.getLevel() + 1;
        if (level > 3) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "category level must not exceed 3");
        }
        if (requestedLevel != null && requestedLevel != level) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "category level does not match parent level");
        }
        return level;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Validate that the requested parent assignment does not create self-parenting or cycles.
     * Params:
     * - categoryId: target category id
     * - parentId: requested parent category id
     * - creating: whether the current flow is category creation
     * Returns: None
     * Throws:
     * - BusinessException: when the requested parent assignment is invalid
     */
    private void validateParentAssignment(Long categoryId, Long parentId, boolean creating) {
        if (parentId == null || creating) {
            return;
        }
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "category cannot be its own parent");
        }
        if (categoryId != null && isDescendantOf(parentId, categoryId)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "category parent cannot be a descendant of itself");
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Detect whether one category currently sits under the specified ancestor in the taxonomy tree.
     * Params:
     * - categoryId: starting category id
     * - expectedAncestorId: ancestor category id to check
     * Returns:
     * - boolean: whether the starting category is a descendant of the ancestor
     * Throws: None
     */
    private boolean isDescendantOf(Long categoryId, Long expectedAncestorId) {
        Long currentId = categoryId;
        while (currentId != null) {
            Category current = categoryMapper.selectById(currentId);
            if (current == null || current.getParentId() == null) {
                return false;
            }
            if (expectedAncestorId.equals(current.getParentId())) {
                return true;
            }
            currentId = current.getParentId();
        }
        return false;
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, errorMessage);
        }
        return normalized;
    }

    private List<CategoryResponse> buildTree(List<Category> categories) {
        Map<Long, CategoryResponse> responseMap = new LinkedHashMap<>();
        for (Category category : categories) {
            responseMap.put(category.getId(), toResponse(category, new ArrayList<>()));
        }
        List<CategoryResponse> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryResponse response = responseMap.get(category.getId());
            if (category.getParentId() == null) {
                roots.add(response);
                continue;
            }
            CategoryResponse parent = responseMap.get(category.getParentId());
            if (parent != null) {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(response);
            } else {
                roots.add(response);
            }
        }
        return roots;
    }

    private CategoryResponse toResponse(Category category, List<CategoryResponse> children) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setParentId(category.getParentId());
        response.setName(category.getName());
        response.setLevel(category.getLevel());
        response.setSortOrder(category.getSortOrder());
        response.setActive(category.getIsActive() != null && category.getIsActive() == 1);
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        response.setChildren(children);
        return response;
    }
}
