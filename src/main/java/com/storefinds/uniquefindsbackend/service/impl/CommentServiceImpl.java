package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCommentRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Inject mapper dependencies for comment business logic.
     * Params:
     * - commentMapper: comment data access mapper
     * - postMapper: post data access mapper
     * Returns: None
     * Throws: None
     */
    public CommentServiceImpl(CommentMapper commentMapper, PostMapper postMapper) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Create one new visible comment under one published post.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * - request: create comment payload
     * Returns:
     * - Result<CommentResponse>: created comment detail
     * Throws:
     * - BusinessException: when post is unavailable, reply target is invalid, or content is blank
     */
    public Result<CommentResponse> createComment(Long userId, Long postId, CreateCommentRequest request) {
        requirePublishedPost(postId);
        Comment parentComment = requireReplyTarget(postId, request.getParentId());

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentComment == null ? null : parentComment.getId());
        comment.setRootId(resolveRootId(parentComment));
        comment.setReplyToUserId(parentComment == null ? null : parentComment.getUserId());
        comment.setContent(normalizeRequiredText(request.getContent(), "content is required"));
        comment.setStatus("VISIBLE");
        commentMapper.insert(comment);

        return Result.success("comment created", toCommentResponse(userId, commentMapper.selectById(comment.getId())));
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query all visible comments under one published post.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<List<CommentResponse>>: comment list
     * Throws:
     * - BusinessException: when post is unavailable
     */
    public Result<PageResponse<CommentResponse>> getCommentsByPostId(Long userId, Long postId, int page, int pageSize) {
        requirePublishedPost(postId);
        return Result.success(buildCommentPage(
                commentMapper.countDisplayableByPostId(postId),
                page,
                pageSize,
                commentMapper.selectDisplayableByPostId(postId, toOffset(page, pageSize), pageSize),
                userId
        ));
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Soft delete one visible comment owned by the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: deletion result
     * Throws:
     * - BusinessException: when post or comment is unavailable, or comment is not owned by user
     */
    public Result<Void> deleteComment(Long userId, Long postId, Long commentId) {
        requireExistingPost(postId);
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !"VISIBLE".equalsIgnoreCase(comment.getStatus()) || !postId.equals(comment.getPostId())) {
            throw new BusinessException("comment not found");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException("you can only operate your own comments");
        }

        commentMapper.softDeleteById(commentId, userId);
        return Result.success("comment deleted", null);
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query one page of comments created by the current user.
     * Params:
     * - userId: current authenticated user id
     * - page: target page number starting from 1
     * - pageSize: target page size
     * Returns:
     * - Result<PageResponse<CommentResponse>>: paged user comment list
     * Throws: None
     */
    public Result<PageResponse<CommentResponse>> getMyComments(Long userId, int page, int pageSize) {
        return Result.success(buildCommentPage(
                commentMapper.countByUserId(userId),
                page,
                pageSize,
                commentMapper.selectByUserId(userId, toOffset(page, pageSize), pageSize),
                userId
        ));
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Ensure the target post exists and is currently published.
     * Params:
     * - postId: target post id
     * Returns:
     * - Post: published post entity
     * Throws:
     * - BusinessException: when post is missing or not published
     */
    private Post requirePublishedPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post is not available");
        }
        return post;
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Ensure the target post exists and is not deleted.
     * Params:
     * - postId: target post id
     * Returns:
     * - Post: matched post entity
     * Throws:
     * - BusinessException: when post is missing or deleted
     */
    private Post requireExistingPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || "DELETED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post not found");
        }
        return post;
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Validate reply target comment under the same post when parentId is provided.
     * Params:
     * - postId: target post id
     * - parentId: optional parent comment id
     * Returns:
     * - Comment: validated parent comment or null
     * Throws:
     * - BusinessException: when reply target is missing or unavailable
     */
    private Comment requireReplyTarget(Long postId, Long parentId) {
        if (parentId == null) {
            return null;
        }

        Comment parentComment = commentMapper.selectById(parentId);
        if (parentComment == null
                || !"VISIBLE".equalsIgnoreCase(parentComment.getStatus())
                || !postId.equals(parentComment.getPostId())) {
            throw new BusinessException("reply target is not available");
        }
        return parentComment;
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Resolve root comment id for one new comment based on its parent comment.
     * Params:
     * - parentComment: validated parent comment
     * Returns:
     * - Long: root comment id or null for top-level comment
     * Throws: None
     */
    private Long resolveRootId(Comment parentComment) {
        if (parentComment == null) {
            return null;
        }
        return parentComment.getRootId() == null ? parentComment.getId() : parentComment.getRootId();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-27
     * Purpose: Trim one required text field and reject blank content.
     * Params:
     * - value: raw text value
     * - errorMessage: exception message when field is blank
     * Returns:
     * - String: normalized text value
     * Throws:
     * - BusinessException: when normalized value is blank
     */
    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(errorMessage);
        }
        return normalized;
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Convert page number and page size into SQL row offset.
     * Params:
     * - page: target page number starting from 1
     * - pageSize: target page size
     * Returns:
     * - int: SQL row offset
     * Throws: None
     */
    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Build one paged comment response object.
     * Params:
     * - total: total matched comment count
     * - page: target page number starting from 1
     * - pageSize: target page size
     * - comments: source comment entity list
     * - currentUserId: current authenticated user id or null
     * Returns:
     * - PageResponse<CommentResponse>: paged response payload
     * Throws: None
     */
    private PageResponse<CommentResponse> buildCommentPage(long total,
                                                           int page,
                                                           int pageSize,
                                                           java.util.List<Comment> comments,
                                                           Long currentUserId) {
        PageResponse<CommentResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(comments.stream()
                .map(comment -> toCommentResponse(currentUserId, comment))
                .toList());
        return response;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-27
     * Purpose: Convert comment entity to API response object.
     * Params:
     * - currentUserId: current authenticated user id
     * - comment: source comment entity
     * Returns:
     * - CommentResponse: response payload object
     * Throws: None
     */
    private CommentResponse toCommentResponse(Long currentUserId, Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPostId());
        response.setPostTitle(comment.getPostTitle());
        response.setUserId(comment.getUserId());
        response.setUsername(comment.getUsername());
        response.setParentId(comment.getParentId());
        response.setRootId(comment.getRootId());
        response.setReplyToUserId(comment.getReplyToUserId());
        response.setReplyToUsername(comment.getReplyToUsername());
        response.setContent("DELETED".equalsIgnoreCase(comment.getStatus()) ? "This comment has been deleted." : comment.getContent());
        response.setStatus(comment.getStatus());
        response.setDeleted("DELETED".equalsIgnoreCase(comment.getStatus()));
        response.setOwnedByCurrentUser(currentUserId != null && currentUserId.equals(comment.getUserId()));
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
