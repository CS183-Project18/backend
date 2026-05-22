package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateTagRequest;
import com.storefinds.uniquefindsbackend.dto.TagResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateTagRequest;

import java.util.List;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Define tag dictionary query and admin maintenance capabilities.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface TagService {

    Result<List<TagResponse>> getTags();

    Result<TagResponse> createTag(CreateTagRequest request);

    Result<TagResponse> updateTag(Long tagId, UpdateTagRequest request);
}
