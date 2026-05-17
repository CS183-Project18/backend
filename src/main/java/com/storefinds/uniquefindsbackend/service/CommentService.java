package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCommentRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Define comment CRUD and interaction capabilities used by user-facing comment APIs.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface CommentService {

    Result<CommentResponse> createComment(Long userId, Long postId, CreateCommentRequest request);

    Result<PageResponse<CommentResponse>> getCommentsByPostId(Long userId, Long postId, int page, int pageSize);

    Result<Void> deleteComment(Long userId, Long postId, Long commentId);

    Result<PageResponse<CommentResponse>> getMyComments(Long userId, int page, int pageSize);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Like one visible comment for the current user.
     * Params:
     * - userId: current user id
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    Result<Void> likeComment(Long userId, Long commentId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Remove current user's like relation from one visible comment.
     * Params:
     * - userId: current user id
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    Result<Void> unlikeComment(Long userId, Long commentId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Pin one comment when the current user is the post owner or an admin.
     * Params:
     * - userId: current user id
     * - userRole: current user role
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    Result<Void> pinComment(Long userId, String userRole, Long commentId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Clear pinned state from one comment when the current user has permission.
     * Params:
     * - userId: current user id
     * - userRole: current user role
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    Result<Void> unpinComment(Long userId, String userRole, Long commentId);
}
