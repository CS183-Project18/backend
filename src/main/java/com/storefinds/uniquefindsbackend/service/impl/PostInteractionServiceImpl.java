package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.InteractionStatusResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.PostInteractionService;
import com.storefinds.uniquefindsbackend.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostInteractionServiceImpl implements PostInteractionService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostService postService;

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Inject mapper and post service dependencies for post interaction logic.
     * Params:
     * - postMapper: post data access mapper
     * - postLikeMapper: post like data access mapper
     * - postFavoriteMapper: post favorite data access mapper
     * - postService: post business service for response assembly
     * Returns: None
     * Throws: None
     */
    public PostInteractionServiceImpl(PostMapper postMapper,
                                      PostLikeMapper postLikeMapper,
                                      PostFavoriteMapper postFavoriteMapper,
                                      PostService postService) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.postService = postService;
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Like one published post for the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is unavailable
     */
    public Result<Void> likePost(Long userId, Long postId) {
        requirePublishedPost(postId);
        postLikeMapper.insertIgnore(userId, postId);
        return Result.success("post liked", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Cancel one like relation for the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is unavailable
     */
    public Result<Void> unlikePost(Long userId, Long postId) {
        requirePublishedPost(postId);
        postLikeMapper.deleteByUserIdAndPostId(userId, postId);
        return Result.success("post unliked", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Favorite one published post for the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is unavailable
     */
    public Result<Void> favoritePost(Long userId, Long postId) {
        requirePublishedPost(postId);
        postFavoriteMapper.insertIgnore(userId, postId);
        return Result.success("post favorited", null);
    }

    @Override
    @Transactional
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Cancel one favorite relation for the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when post is unavailable
     */
    public Result<Void> unfavoritePost(Long userId, Long postId) {
        requirePublishedPost(postId);
        postFavoriteMapper.deleteByUserIdAndPostId(userId, postId);
        return Result.success("post unfavorited", null);
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query current user's like and favorite status on one published post.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<InteractionStatusResponse>: interaction status payload
     * Throws:
     * - BusinessException: when post is unavailable
     */
    public Result<InteractionStatusResponse> getInteractionStatus(Long userId, Long postId) {
        requirePublishedPost(postId);

        InteractionStatusResponse response = new InteractionStatusResponse();
        response.setPostId(postId);
        response.setLiked(postLikeMapper.countByUserIdAndPostId(userId, postId) > 0);
        response.setFavorited(postFavoriteMapper.countByUserIdAndPostId(userId, postId) > 0);
        return Result.success(response);
    }

    @Override
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query all visible favorited posts of the current user.
     * Params:
     * - userId: current authenticated user id
     * Returns:
     * - Result<List<PostResponse>>: favorite post list
     * Throws: None
     */
    public Result<PageResponse<PostResponse>> getMyFavoritePosts(Long userId, int page, int pageSize) {
        List<Post> posts = postFavoriteMapper.selectFavoritePostsByUserIdPage(userId, toOffset(page, pageSize), pageSize);
        PageResponse<PostResponse> response = new PageResponse<>();
        response.setTotal(postFavoriteMapper.countFavoritePostsByUserId(userId));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(postService.buildPostResponsesForUser(userId, posts));
        return Result.success(response);
    }

    /**
     * Author: Kaijie Zhu
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
     * Author: Kaijie Zhu
     * Date: 2026-05-06
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
}
