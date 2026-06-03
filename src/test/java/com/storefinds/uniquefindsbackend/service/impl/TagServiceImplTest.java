package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.CreateTagRequest;
import com.storefinds.uniquefindsbackend.dto.UpdateTagRequest;
import com.storefinds.uniquefindsbackend.entity.Tag;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.TagMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Verify tag management behavior used in discovery and search flows.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private IndexSyncService indexSyncService;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    void createTagRejectsDuplicateName() {
        Tag existing = new Tag();
        existing.setId(3L);
        existing.setName("retro");
        when(tagMapper.selectByName("retro")).thenReturn(existing);

        CreateTagRequest request = new CreateTagRequest();
        request.setName("retro");

        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.createTag(request));

        assertEquals("tag name already exists", ex.getMessage());
    }

    @Test
    void updateTagRejectsDuplicateNameOwnedByAnotherTag() {
        Tag existing = new Tag();
        existing.setId(3L);
        existing.setName("retro");

        Tag duplicate = new Tag();
        duplicate.setId(4L);
        duplicate.setName("cozy");

        when(tagMapper.selectById(3L)).thenReturn(existing);
        when(tagMapper.selectByName("cozy")).thenReturn(duplicate);

        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("cozy");

        BusinessException ex = assertThrows(BusinessException.class, () -> tagService.updateTag(3L, request));

        assertEquals("tag name already exists", ex.getMessage());
    }
}
