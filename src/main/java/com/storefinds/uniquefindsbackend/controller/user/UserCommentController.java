package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.CommentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/comments")
public class UserCommentController {

    private final CommentService commentService;

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Inject comment service for personal comment endpoints.
     * Params:
     * - commentService: comment business service
     * Returns: None
     * Throws: None
     */
    public UserCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/mine")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query one page of comments created by the current authenticated user.
     * Params:
     * - page: target page number starting from 1
     * - pageSize: target page size
     * - authentication: spring authentication object
     * Returns:
     * - Result<PageResponse<CommentResponse>>: paged personal comment list
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<PageResponse<CommentResponse>> getMyComments(@RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                               @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                               Authentication authentication) {
        return commentService.getMyComments(requireCurrentUser(authentication).userId(), page, pageSize);
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Extract current authenticated user from spring security context.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - CurrentUser: authenticated principal wrapper
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    private CurrentUser requireCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return currentUser;
    }
}
