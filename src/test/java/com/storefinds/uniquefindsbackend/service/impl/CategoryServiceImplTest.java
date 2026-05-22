package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.CreateCategoryRequest;
import com.storefinds.uniquefindsbackend.dto.UpdateCategoryRequest;
import com.storefinds.uniquefindsbackend.entity.Category;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CategoryMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private IndexSyncService indexSyncService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategoryRejectsRootLevelOtherThanOne() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Desk Lamps");
        request.setLevel(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.createCategory(request));

        assertEquals("root category level must be 1", ex.getMessage());
    }

    @Test
    void updateCategoryRejectsSelfParent() {
        Category existing = new Category();
        existing.setId(10L);
        existing.setLevel(1);
        existing.setIsActive(1);
        when(categoryMapper.selectById(10L)).thenReturn(existing);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setParentId(10L);
        request.setName("Desk Lamps");
        request.setLevel(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.updateCategory(10L, request));

        assertEquals("category cannot be its own parent", ex.getMessage());
    }

    @Test
    void updateCategoryRejectsDescendantAsParent() {
        Category existing = new Category();
        existing.setId(10L);
        existing.setLevel(1);
        existing.setIsActive(1);

        Category child = new Category();
        child.setId(11L);
        child.setParentId(10L);
        child.setLevel(2);
        child.setIsActive(1);

        when(categoryMapper.selectById(10L)).thenReturn(existing);
        when(categoryMapper.selectById(11L)).thenReturn(child);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setParentId(11L);
        request.setName("Desk Lamps");
        request.setLevel(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.updateCategory(10L, request));

        assertEquals("category parent cannot be a descendant of itself", ex.getMessage());
    }

    @Test
    void updateCategoryRejectsInactiveParent() {
        Category existing = new Category();
        existing.setId(10L);
        existing.setLevel(1);
        existing.setIsActive(1);

        Category inactiveParent = new Category();
        inactiveParent.setId(12L);
        inactiveParent.setLevel(1);
        inactiveParent.setIsActive(0);

        when(categoryMapper.selectById(10L)).thenReturn(existing);
        when(categoryMapper.selectById(12L)).thenReturn(inactiveParent);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setParentId(12L);
        request.setName("Desk Lamps");
        request.setLevel(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> categoryService.updateCategory(10L, request));

        assertEquals("parent category is not active", ex.getMessage());
    }
}
