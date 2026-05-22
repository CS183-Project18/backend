package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.TagResponse;
import com.storefinds.uniquefindsbackend.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Expose public tag listing endpoint for structured post creation and discovery filters.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class TagController {

    private final TagService tagService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Inject tag service for public tag query endpoints.
     * Params:
     * - tagService: tag business service
     * Returns: None
     * Throws: None
     */
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Query all tags for public selection and discovery use cases.
     * Params: None
     * Returns:
     * - Result<List<TagResponse>>: ordered tag list
     * Throws: None
     */
    public Result<List<TagResponse>> getTags() {
        return tagService.getTags();
    }
}
