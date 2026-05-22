package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CategoryResponse;
import com.storefinds.uniquefindsbackend.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose public category query endpoints for frontend filters and taxonomy rendering.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject category service for public taxonomy query endpoints.
     * Params:
     * - categoryService: category business service
     * Returns: None
     * Throws: None
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query a flat category list for frontend selection components.
     * Params:
     * - activeOnly: whether to keep only active categories
     * Returns:
     * - Result<List<CategoryResponse>>: flat category list
     * Throws: None
     */
    public Result<List<CategoryResponse>> getCategories(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return categoryService.getCategories(activeOnly);
    }

    @GetMapping("/tree")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query a tree-structured category response for frontend taxonomy views.
     * Params:
     * - activeOnly: whether to keep only active categories
     * Returns:
     * - Result<List<CategoryResponse>>: category tree
     * Throws: None
     */
    public Result<List<CategoryResponse>> getCategoryTree(@RequestParam(defaultValue = "true") boolean activeOnly) {
        return categoryService.getCategoryTree(activeOnly);
    }
}
