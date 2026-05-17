package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.NotificationEventType;
import com.storefinds.uniquefindsbackend.common.NotificationTargetType;
import com.storefinds.uniquefindsbackend.common.ReportTargetType;
import com.storefinds.uniquefindsbackend.dto.InteractionStatusResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.SharePostResponse;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.PostInteractionService;
import com.storefinds.uniquefindsbackend.service.PostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-12
 * Purpose: Implement post likes, favorites, interaction status lookup, and share metadata generation.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class PostInteractionServiceImpl implements PostInteractionService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostService postService;
    private final NotificationService notificationService;
    private final InteractionEventService interactionEventService;
    private final String publicBaseUrl;

    public PostInteractionServiceImpl(PostMapper postMapper,
                                      PostLikeMapper postLikeMapper,
                                      PostFavoriteMapper postFavoriteMapper,
                                      PostService postService,
                                      NotificationService notificationService,
                                      InteractionEventService interactionEventService,
                                      @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.postService = postService;
        this.notificationService = notificationService;
        this.interactionEventService = interactionEventService;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Like one published post and create a notification only on the first successful relation insert.
     * Params:
     * - userId: current user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the post is not publicly visible
     */
    public Result<Void> likePost(Long userId, Long postId) {
        Post post = requirePublishedPost(postId);
        if (postLikeMapper.insertIgnore(userId, postId) > 0) {
            interactionEventService.record(InteractionEventType.POST_LIKE, userId, postId, null, ReportTargetType.POST, postId, null);
            notificationService.createNotification(post.getUserId(),
                    userId,
                    NotificationEventType.POST_LIKED,
                    NotificationTargetType.POST,
                    postId,
                    postId);
        }
        return Result.success("post liked", null);
    }

    @Override
    @Transactional
    public Result<Void> unlikePost(Long userId, Long postId) {
        requirePublishedPost(postId);
        if (postLikeMapper.deleteByUserIdAndPostId(userId, postId) > 0) {
            interactionEventService.record(InteractionEventType.POST_UNLIKE, userId, postId, null, ReportTargetType.POST, postId, null);
        }
        return Result.success("post unliked", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Favorite one published post and create a notification only on the first successful relation insert.
     * Params:
     * - userId: current user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the post is not publicly visible
     */
    public Result<Void> favoritePost(Long userId, Long postId) {
        Post post = requirePublishedPost(postId);
        if (postFavoriteMapper.insertIgnore(userId, postId) > 0) {
            interactionEventService.record(InteractionEventType.POST_FAVORITE, userId, postId, null, ReportTargetType.POST, postId, null);
            notificationService.createNotification(post.getUserId(),
                    userId,
                    NotificationEventType.POST_FAVORITED,
                    NotificationTargetType.POST,
                    postId,
                    postId);
        }
        return Result.success("post favorited", null);
    }

    @Override
    @Transactional
    public Result<Void> unfavoritePost(Long userId, Long postId) {
        requirePublishedPost(postId);
        if (postFavoriteMapper.deleteByUserIdAndPostId(userId, postId) > 0) {
            interactionEventService.record(InteractionEventType.POST_UNFAVORITE, userId, postId, null, ReportTargetType.POST, postId, null);
        }
        return Result.success("post unfavorited", null);
    }

    @Override
    public Result<InteractionStatusResponse> getInteractionStatus(Long userId, Long postId) {
        requirePublishedPost(postId);

        InteractionStatusResponse response = new InteractionStatusResponse();
        response.setPostId(postId);
        response.setLiked(postLikeMapper.countByUserIdAndPostId(userId, postId) > 0);
        response.setFavorited(postFavoriteMapper.countByUserIdAndPostId(userId, postId) > 0);
        return Result.success(response);
    }

    @Override
    public Result<PageResponse<PostResponse>> getMyFavoritePosts(Long userId, int page, int pageSize) {
        List<Post> posts = postFavoriteMapper.selectFavoritePostsByUserIdPage(userId, toOffset(page, pageSize), pageSize);
        PageResponse<PostResponse> response = new PageResponse<>();
        response.setTotal(postFavoriteMapper.countFavoritePostsByUserId(userId));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(postService.buildPostResponsesForUser(userId, posts));
        return Result.success(response);
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Build canonical share metadata for one published post.
     * Params:
     * - postId: target post id
     * Returns:
     * - Result<SharePostResponse>: canonical share data
     * Throws:
     * - BusinessException: when the post is not publicly visible
     */
    public Result<SharePostResponse> sharePost(Long postId) {
        Post post = requirePublishedPost(postId);
        interactionEventService.record(
                InteractionEventType.SHARE_LINK_CREATE,
                null,
                postId,
                null,
                ReportTargetType.POST,
                postId,
                Map.of("shareUrl", buildShareUrl(postId))
        );
        return Result.success(new SharePostResponse(post.getId(), buildShareUrl(post.getId())));
    }

    private Post requirePublishedPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post is not available");
        }
        return post;
    }

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    private String buildShareUrl(Long postId) {
        return publicBaseUrl.replaceAll("/+$", "") + "/posts/" + postId;
    }
}
