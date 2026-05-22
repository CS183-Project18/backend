package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateTagRequest;
import com.storefinds.uniquefindsbackend.dto.TagResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateTagRequest;
import com.storefinds.uniquefindsbackend.entity.Tag;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.TagMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import com.storefinds.uniquefindsbackend.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Implement tag dictionary query and maintenance flows with duplicate-name protection.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final IndexSyncService indexSyncService;

    public TagServiceImpl(TagMapper tagMapper,
                          IndexSyncService indexSyncService) {
        this.tagMapper = tagMapper;
        this.indexSyncService = indexSyncService;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query all tags for frontend selection and discovery views.
     * Params: None
     * Returns:
     * - Result<List<TagResponse>>: ordered tag list
     * Throws: None
     */
    public Result<List<TagResponse>> getTags() {
        return Result.success(tagMapper.selectAll().stream().map(this::toResponse).toList());
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Create one tag dictionary record for admin maintenance.
     * Params:
     * - request: create tag payload
     * Returns:
     * - Result<TagResponse>: created tag detail
     * Throws:
     * - BusinessException: when tag name is blank or already exists
     */
    public Result<TagResponse> createTag(CreateTagRequest request) {
        String name = normalizeRequiredText(request.getName(), "name is required");
        if (tagMapper.selectByName(name) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "tag name already exists");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setHeatScore(BigDecimal.ZERO);
        tagMapper.insert(tag);
        return Result.success("tag created", toResponse(tagMapper.selectById(tag.getId())));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one existing tag including optional heat score metadata.
     * Params:
     * - tagId: target tag id
     * - request: update tag payload
     * Returns:
     * - Result<TagResponse>: updated tag detail
     * Throws:
     * - BusinessException: when target tag does not exist or name conflicts
     */
    public Result<TagResponse> updateTag(Long tagId, UpdateTagRequest request) {
        Tag existing = requireTag(tagId);
        String name = normalizeRequiredText(request.getName(), "name is required");
        Tag duplicate = tagMapper.selectByName(name);
        if (duplicate != null && !duplicate.getId().equals(tagId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "tag name already exists");
        }
        existing.setName(name);
        existing.setHeatScore(request.getHeatScore() == null ? existing.getHeatScore() : request.getHeatScore());
        tagMapper.updateById(existing);
        indexSyncService.scheduleRebuild("tag updated: tagId=" + tagId);
        return Result.success("tag updated", toResponse(tagMapper.selectById(tagId)));
    }

    private Tag requireTag(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "tag not found");
        }
        return tag;
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, errorMessage);
        }
        return normalized;
    }

    private TagResponse toResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setHeatScore(tag.getHeatScore());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }
}
