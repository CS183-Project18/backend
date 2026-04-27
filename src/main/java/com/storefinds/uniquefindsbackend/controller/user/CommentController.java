package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCommentRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Inject comment service for comment interaction endpoints.
     * Params:
     * - commentService: comment business service
     * Returns: None
     * Throws: None
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Create one new comment under the specified post.
     * Params:
     * - postId: target post id
     * - request: create comment payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<CommentResponse>: created comment detail
     * Throws:
     * - BusinessException: when current request is unauthenticated or comment payload is invalid
     */
    public Result<CommentResponse> createComment(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                                 @RequestBody @Valid CreateCommentRequest request,
                                                 Authentication authentication) {
        return commentService.createComment(requireCurrentUser(authentication).userId(), postId, request);
    }

    @GetMapping
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query the visible comment list under the specified post.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<List<CommentResponse>>: visible comment list
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is not accessible
     */
    public Result<PageResponse<CommentResponse>> getComments(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                                             @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                             @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                             Authentication authentication) {
        Long currentUserId = extractCurrentUserId(authentication);
        return commentService.getCommentsByPostId(currentUserId, postId, page, pageSize);
    }

    @DeleteMapping("/{commentId}")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Soft delete one comment owned by the current authenticated user.
     * Params:
     * - postId: target post id
     * - commentId: target comment id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: deletion result
     * Throws:
     * - BusinessException: when current request is unauthenticated or comment is not owned by user
     */
    public Result<Void> deleteComment(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                      @PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                      Authentication authentication) {
        return commentService.deleteComment(requireCurrentUser(authentication).userId(), postId, commentId);
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

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Extract current authenticated user id when request may come from guest.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Long: authenticated user id or null
     * Throws: None
     */
    private Long extractCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return currentUser.userId();
    }
}
