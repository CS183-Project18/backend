package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.CommentService;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/comments")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-11
 * Purpose: Expose comment interaction endpoints including like and pin operations.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CommentInteractionController {

    private final CommentService commentService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Inject comment service for comment interaction endpoints.
     * Params:
     * - commentService: comment business service
     * Returns: None
     * Throws: None
     */
    public CommentInteractionController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{commentId}/like")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Like one visible comment for the current authenticated user.
     * Params:
     * - commentId: target comment id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<Void> likeComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                    Authentication authentication) {
        return commentService.likeComment(requireCurrentUser(authentication).userId(), commentId);
    }

    @DeleteMapping("/{commentId}/like")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Remove current user's like relation from one comment.
     * Params:
     * - commentId: target comment id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<Void> unlikeComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                      Authentication authentication) {
        return commentService.unlikeComment(requireCurrentUser(authentication).userId(), commentId);
    }

    @PostMapping("/{commentId}/pin")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Pin one comment when the current user is the post owner or an admin.
     * Params:
     * - commentId: target comment id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or unauthorized
     */
    public Result<Void> pinComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                   Authentication authentication) {
        CurrentUser currentUser = requireCurrentUser(authentication);
        return commentService.pinComment(currentUser.userId(), currentUser.role(), commentId);
    }

    @DeleteMapping("/{commentId}/pin")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Clear pinned state from one comment when the current user has permission.
     * Params:
     * - commentId: target comment id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or unauthorized
     */
    public Result<Void> unpinComment(@PathVariable @Min(value = 1, message = "commentId must be greater than 0") Long commentId,
                                     Authentication authentication) {
        CurrentUser currentUser = requireCurrentUser(authentication);
        return commentService.unpinComment(currentUser.userId(), currentUser.role(), commentId);
    }

    private CurrentUser requireCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return currentUser;
    }
}
