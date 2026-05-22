package com.storefinds.uniquefindsbackend.controller.admin;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreateTagRequest;
import com.storefinds.uniquefindsbackend.dto.TagResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateTagRequest;
import com.storefinds.uniquefindsbackend.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/tags")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose admin tag maintenance endpoints for tag dictionary management.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class AdminTagController {

    private final TagService tagService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject tag service for admin tag maintenance endpoints.
     * Params:
     * - tagService: tag business service
     * Returns: None
     * Throws: None
     */
    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Create one tag dictionary entry through the admin console.
     * Params:
     * - request: create tag payload
     * Returns:
     * - Result<TagResponse>: created tag detail
     * Throws: None
     */
    public Result<TagResponse> createTag(@RequestBody @Valid CreateTagRequest request) {
        return tagService.createTag(request);
    }

    @PutMapping("/{tagId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Update one tag dictionary entry and optional heat score.
     * Params:
     * - tagId: target tag id
     * - request: update tag payload
     * Returns:
     * - Result<TagResponse>: updated tag detail
     * Throws: None
     */
    public Result<TagResponse> updateTag(@PathVariable @Min(1) Long tagId,
                                         @RequestBody @Valid UpdateTagRequest request) {
        return tagService.updateTag(tagId, request);
    }
}
