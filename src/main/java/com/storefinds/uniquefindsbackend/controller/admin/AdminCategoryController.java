package com.storefinds.uniquefindsbackend.controller.admin;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CategoryResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCategoryRequest;
import com.storefinds.uniquefindsbackend.dto.UpdateCategoryRequest;
import com.storefinds.uniquefindsbackend.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/categories")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose admin category maintenance endpoints for structured taxonomy management.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject category service for admin taxonomy maintenance endpoints.
     * Params:
     * - categoryService: category business service
     * Returns: None
     * Throws: None
     */
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Create one category node for admin taxonomy maintenance.
     * Params:
     * - request: create category payload
     * Returns:
     * - Result<CategoryResponse>: created category detail
     * Throws: None
     */
    public Result<CategoryResponse> createCategory(@RequestBody @Valid CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{categoryId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one category node in the admin taxonomy console.
     * Params:
     * - categoryId: target category id
     * - request: update category payload
     * Returns:
     * - Result<CategoryResponse>: updated category detail
     * Throws: None
     */
    public Result<CategoryResponse> updateCategory(@PathVariable @Min(1) Long categoryId,
                                                   @RequestBody @Valid UpdateCategoryRequest request) {
        return categoryService.updateCategory(categoryId, request);
    }

    @PutMapping("/{categoryId}/active")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Enable or disable one category for future frontend selection.
     * Params:
     * - categoryId: target category id
     * - active: target active flag
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    public Result<Void> updateCategoryActive(@PathVariable @Min(1) Long categoryId,
                                             @RequestParam boolean active) {
        return categoryService.updateCategoryActive(categoryId, active);
    }
}
