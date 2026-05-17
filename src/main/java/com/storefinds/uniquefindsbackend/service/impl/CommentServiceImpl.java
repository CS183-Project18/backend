package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.CommentStatus;
import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.NotificationEventType;
import com.storefinds.uniquefindsbackend.common.NotificationTargetType;
import com.storefinds.uniquefindsbackend.common.PostStatus;
import com.storefinds.uniquefindsbackend.common.ReportTargetType;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCommentRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.entity.Comment;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.CommentService;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-12
 * Purpose: Implement comment CRUD, visibility-aware comment listing, comment likes, and comment pinning.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final NotificationService notificationService;
    private final InteractionEventService interactionEventService;

    public CommentServiceImpl(CommentMapper commentMapper,
                              PostMapper postMapper,
                              CommentLikeMapper commentLikeMapper,
                              NotificationService notificationService,
                              InteractionEventService interactionEventService) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.notificationService = notificationService;
        this.interactionEventService = interactionEventService;
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Create one comment or reply under a published post and emit reply notifications when needed.
     * Params:
     * - userId: current user id
     * - postId: target post id
     * - request: create comment payload
     * Returns:
     * - Result<CommentResponse>: created comment detail
     * Throws:
     * - BusinessException: when the post or reply target is invalid
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
        comment.setStatus(CommentStatus.VISIBLE);
        comment.setIsPinned(0);
        commentMapper.insert(comment);
        interactionEventService.record(
                InteractionEventType.COMMENT_CREATE,
                userId,
                postId,
                comment.getId(),
                ReportTargetType.COMMENT,
                comment.getId(),
                buildMetadata("parentId", request.getParentId())
        );

        Comment created = commentMapper.selectById(comment.getId());
        if (parentComment != null) {
            notificationService.createNotification(parentComment.getUserId(),
                    userId,
                    NotificationEventType.COMMENT_REPLIED,
                    NotificationTargetType.COMMENT,
                    created.getId(),
                    created.getPostId());
        }

        return Result.success("comment created", toCommentResponse(userId, created, Set.of()));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Query displayable comments for one published post and attach current-user interaction state.
     * Params:
     * - userId: current user id, nullable for guests
     * - postId: target post id
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - Result<PageResponse<CommentResponse>>: comment page
     * Throws:
     * - BusinessException: when the post is not publicly visible
     */
    public Result<PageResponse<CommentResponse>> getCommentsByPostId(Long userId, Long postId, int page, int pageSize) {
        requirePublishedPost(postId);
        List<Comment> comments = commentMapper.selectDisplayableByPostId(postId, toOffset(page, pageSize), pageSize);
        return Result.success(buildCommentPage(
                commentMapper.countDisplayableByPostId(postId),
                page,
                pageSize,
                comments,
                userId
        ));
    }

    @Override
    @Transactional
    public Result<Void> deleteComment(Long userId, Long postId, Long commentId) {
        requireExistingPost(postId);
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !CommentStatus.VISIBLE.equalsIgnoreCase(comment.getStatus()) || !postId.equals(comment.getPostId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "comment not found");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "you can only operate your own comments");
        }

        commentMapper.softDeleteById(commentId, userId);
        interactionEventService.record(
                InteractionEventType.COMMENT_DELETE,
                userId,
                postId,
                commentId,
                ReportTargetType.COMMENT,
                commentId,
                buildMetadata("deletedByOwner", true)
        );
        return Result.success("comment deleted", null);
    }

    @Override
    public Result<PageResponse<CommentResponse>> getMyComments(Long userId, int page, int pageSize) {
        List<Comment> comments = commentMapper.selectByUserId(userId, toOffset(page, pageSize), pageSize);
        return Result.success(buildCommentPage(
                commentMapper.countByUserId(userId),
                page,
                pageSize,
                comments,
                userId
        ));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Like one visible comment and create a notification on the first successful relation insert.
     * Params:
     * - userId: current user id
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the comment is not visible
     */
    public Result<Void> likeComment(Long userId, Long commentId) {
        Comment comment = requireVisibleComment(commentId);
        if (commentLikeMapper.insertIgnore(userId, commentId) > 0) {
            notificationService.createNotification(comment.getUserId(),
                    userId,
                    NotificationEventType.COMMENT_LIKED,
                    NotificationTargetType.COMMENT,
                    commentId,
                    comment.getPostId());
        }
        return Result.success("comment liked", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Remove current user's like relation from one visible comment.
     * Params:
     * - userId: current user id
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the comment is not visible
     */
    public Result<Void> unlikeComment(Long userId, Long commentId) {
        requireVisibleComment(commentId);
        commentLikeMapper.deleteByUserIdAndCommentId(userId, commentId);
        return Result.success("comment unliked", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Pin one comment under a post when the current user is the post owner or an admin.
     * Params:
     * - userId: current user id
     * - userRole: current user role
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the user has no permission or the comment is unavailable
     */
    public Result<Void> pinComment(Long userId, String userRole, Long commentId) {
        Comment comment = requireVisibleComment(commentId);
        validatePinPermission(userId, userRole, comment);
        commentMapper.clearPinnedByPostId(comment.getPostId());
        commentMapper.updatePinnedById(commentId, 1);
            notificationService.createNotification(comment.getUserId(),
                    userId,
                    NotificationEventType.COMMENT_PINNED,
                    NotificationTargetType.COMMENT,
                    commentId,
                    comment.getPostId());
        return Result.success("comment pinned", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Clear the pinned state from one comment when the current user has permission.
     * Params:
     * - userId: current user id
     * - userRole: current user role
     * - commentId: target comment id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the user has no permission or the comment is unavailable
     */
    public Result<Void> unpinComment(Long userId, String userRole, Long commentId) {
        Comment comment = requireComment(commentId);
        validatePinPermission(userId, userRole, comment);
        commentMapper.updatePinnedById(commentId, 0);
        return Result.success("comment unpinned", null);
    }

    private Post requirePublishedPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post is not available");
        }
        return post;
    }

    private Post requireExistingPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || PostStatus.DELETED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "post not found");
        }
        return post;
    }

    private Comment requireReplyTarget(Long postId, Long parentId) {
        if (parentId == null) {
            return null;
        }

        Comment parentComment = commentMapper.selectById(parentId);
        if (parentComment == null
                || !CommentStatus.VISIBLE.equalsIgnoreCase(parentComment.getStatus())
                || !postId.equals(parentComment.getPostId())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "reply target is not available");
        }
        return parentComment;
    }

    private Comment requireComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || CommentStatus.DELETED.equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "comment not found");
        }
        return comment;
    }

    private Comment requireVisibleComment(Long commentId) {
        Comment comment = requireComment(commentId);
        if (!CommentStatus.VISIBLE.equalsIgnoreCase(comment.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "comment is not available");
        }
        return comment;
    }

    private void validatePinPermission(Long userId, String userRole, Comment comment) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isPostOwner = userId != null && userId.equals(comment.getPostOwnerId());
        if (!isAdmin && !isPostOwner) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "you cannot pin this comment");
        }
    }

    private Long resolveRootId(Comment parentComment) {
        if (parentComment == null) {
            return null;
        }
        return parentComment.getRootId() == null ? parentComment.getId() : parentComment.getRootId();
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, errorMessage);
        }
        return normalized;
    }

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    private PageResponse<CommentResponse> buildCommentPage(long total,
                                                           int page,
                                                           int pageSize,
                                                           List<Comment> comments,
                                                           Long currentUserId) {
        Set<Long> likedCommentIds = resolveLikedCommentIds(currentUserId, comments);
        PageResponse<CommentResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(comments.stream()
                .map(comment -> toCommentResponse(currentUserId, comment, likedCommentIds))
                .toList());
        return response;
    }

    private Set<Long> resolveLikedCommentIds(Long currentUserId, List<Comment> comments) {
        if (currentUserId == null || comments == null || comments.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(commentLikeMapper.selectLikedCommentIds(
                currentUserId,
                comments.stream().map(Comment::getId).toList()
        ));
    }

    private CommentResponse toCommentResponse(Long currentUserId, Comment comment, Set<Long> likedCommentIds) {
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
        response.setContent(CommentStatus.DELETED.equalsIgnoreCase(comment.getStatus()) ? "This comment has been deleted." : comment.getContent());
        response.setStatus(comment.getStatus());
        response.setLikeCount(comment.getLikeCount());
        response.setLikedByCurrentUser(likedCommentIds.contains(comment.getId()));
        response.setPinned(comment.getIsPinned() != null && comment.getIsPinned() == 1);
        response.setDeleted(CommentStatus.DELETED.equalsIgnoreCase(comment.getStatus()));
        response.setOwnedByCurrentUser(currentUserId != null && currentUserId.equals(comment.getUserId()));
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }

    private Map<String, Object> buildMetadata(Object... keyValues) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object value = keyValues[i + 1];
            if (value != null) {
                metadata.put(String.valueOf(keyValues[i]), value);
            }
        }
        return metadata;
    }
}
