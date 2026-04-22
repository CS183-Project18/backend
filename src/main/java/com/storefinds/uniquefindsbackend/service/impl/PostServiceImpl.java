package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreatePostRequest;
import com.storefinds.uniquefindsbackend.dto.PostImageRequest;
import com.storefinds.uniquefindsbackend.dto.PostImageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.UpdatePostRequest;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-17
     * Purpose: Inject post mapper dependencies for post CRUD business logic.
     * Params:
     * - postMapper: post data access mapper
     * - postImageMapper: post image data access mapper
     * Returns: None
     * Throws: None
     */
    public PostServiceImpl(PostMapper postMapper, PostImageMapper postImageMapper) {
        this.postMapper = postMapper;
        this.postImageMapper = postImageMapper;
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Create a new post for the current user.
     * Params:
     * - userId: current authenticated user id
     * - request: create post payload
     * Returns:
     * - Result<PostResponse>: created post detail
     * Throws:
     * - BusinessException: when request content is invalid
     */
    public Result<PostResponse> createPost(Long userId, CreatePostRequest request) {
        validatePriceRange(request.getPriceMin(), request.getPriceMax());

        Post post = new Post();
        post.setUserId(userId);
        post.setStoreId(request.getStoreId());
        post.setCategoryId(request.getCategoryId());
        post.setTitle(normalizeRequiredText(request.getTitle(), "title is required"));
        post.setDescription(normalizeRequiredText(request.getDescription(), "description is required"));
        post.setPriceMin(request.getPriceMin());
        post.setPriceMax(request.getPriceMax());
        post.setCurrency(normalizeCurrency(request.getCurrency()));
        post.setLocationText(normalizeOptionalText(request.getLocationText()));
        post.setStatus("PENDING_REVIEW");
        postMapper.insert(post);
        replacePostImages(post.getId(), request.getImages());

        Post createdPost = postMapper.selectById(post.getId());
        return Result.success("post created", toPostResponse(createdPost));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Query one post detail visible to the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<PostResponse>: post detail
     * Throws:
     * - BusinessException: when post is missing or not accessible
     */
    public Result<PostResponse> getPostById(Long userId, Long postId) {
        Post post = requireAccessiblePost(userId, postId);
        if ("PUBLISHED".equalsIgnoreCase(post.getStatus())) {
            postMapper.increaseViewCount(postId);
            post = postMapper.selectById(postId);
        }
        return Result.success(toPostResponse(post));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Query all published posts ordered by publish time.
     * Params: None
     * Returns:
     * - Result<List<PostResponse>>: published post list
     * Throws: None
     */
    public Result<List<PostResponse>> getPublishedPosts() {
        List<Post> posts = postMapper.selectPublishedPosts();
        return Result.success(toPostResponses(posts));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Query all non-deleted posts created by the current user.
     * Params:
     * - userId: current authenticated user id
     * Returns:
     * - Result<List<PostResponse>>: current user's post list
     * Throws: None
     */
    public Result<List<PostResponse>> getMyPosts(Long userId) {
        return Result.success(toPostResponses(postMapper.selectByUserId(userId)));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Update one post owned by the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * - request: update payload
     * Returns:
     * - Result<PostResponse>: updated post detail
     * Throws:
     * - BusinessException: when post is missing, forbidden, or request is invalid
     */
    public Result<PostResponse> updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post existingPost = requireOwnedPost(userId, postId);
        validatePriceRange(request.getPriceMin(), request.getPriceMax());

        existingPost.setStoreId(request.getStoreId());
        existingPost.setCategoryId(request.getCategoryId());
        existingPost.setTitle(normalizeRequiredText(request.getTitle(), "title is required"));
        existingPost.setDescription(normalizeRequiredText(request.getDescription(), "description is required"));
        existingPost.setPriceMin(request.getPriceMin());
        existingPost.setPriceMax(request.getPriceMax());
        existingPost.setCurrency(normalizeCurrency(request.getCurrency()));
        existingPost.setLocationText(normalizeOptionalText(request.getLocationText()));
        postMapper.updateById(existingPost);
        replacePostImages(postId, request.getImages());

        Post updatedPost = postMapper.selectById(postId);
        return Result.success("post updated", toPostResponse(updatedPost));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Soft delete one post owned by the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Result<Void>: deletion result
     * Throws:
     * - BusinessException: when post is missing or forbidden
     */
    public Result<Void> deletePost(Long userId, Long postId) {
        requireOwnedPost(userId, postId);
        postMapper.softDeleteById(postId, userId);
        return Result.success("post deleted", null);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Ensure the target post exists and belongs to the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Post: matched owned post
     * Throws:
     * - BusinessException: when post is missing, deleted, or forbidden
     */
    private Post requireOwnedPost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || "DELETED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post not found");
        }
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException("you can only operate your own posts");
        }
        return post;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Ensure the target post can be viewed by the current user.
     * Params:
     * - userId: current authenticated user id
     * - postId: target post id
     * Returns:
     * - Post: accessible post detail
     * Throws:
     * - BusinessException: when post is missing or not publicly visible
     */
    private Post requireAccessiblePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || "DELETED".equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException("post not found");
        }
        boolean isOwner = userId.equals(post.getUserId());
        boolean isPublished = "PUBLISHED".equalsIgnoreCase(post.getStatus());
        if (!isOwner && !isPublished) {
            throw new BusinessException("post is not available");
        }
        return post;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Validate min/max price relationship before persistence.
     * Params:
     * - priceMin: minimum price
     * - priceMax: maximum price
     * Returns: None
     * Throws:
     * - BusinessException: when priceMin is greater than priceMax
     */
    private void validatePriceRange(BigDecimal priceMin, BigDecimal priceMax) {
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            throw new BusinessException("priceMin cannot be greater than priceMax");
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-17
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
     * Author: Kaijie Zhu
     * Date: 2026-04-17
     * Purpose: Trim one optional text field and convert blank to null.
     * Params:
     * - value: raw text value
     * Returns:
     * - String: normalized text or null
     * Throws: None
     */
    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Normalize currency value and apply default when omitted.
     * Params:
     * - currency: raw currency code
     * Returns:
     * - String: upper-cased 3-letter currency code
     * Throws: None
     */
    private String normalizeCurrency(String currency) {
        String normalized = normalizeOptionalText(currency);
        return normalized == null ? "CNY" : normalized.toUpperCase();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Replace all images of one post with the latest request payload.
     * Params:
     * - postId: target post id
     * - imageRequests: latest image payload list
     * Returns: None
     * Throws:
     * - BusinessException: when more than 9 images are provided
     */
    private void replacePostImages(Long postId, List<PostImageRequest> imageRequests) {
        postImageMapper.deleteByPostId(postId);
        List<PostImage> images = buildPostImages(postId, imageRequests);
        if (!images.isEmpty()) {
            postImageMapper.batchInsert(images);
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Convert request image payloads into post image entities.
     * Params:
     * - postId: target post id
     * - imageRequests: request image list
     * Returns:
     * - List<PostImage>: normalized image entities
     * Throws:
     * - BusinessException: when too many images are provided
     */
    private List<PostImage> buildPostImages(Long postId, List<PostImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return Collections.emptyList();
        }
        if (imageRequests.size() > 9) {
            throw new BusinessException("a post can contain at most 9 images");
        }

        List<PostImage> images = new ArrayList<>();
        for (int i = 0; i < imageRequests.size(); i++) {
            PostImageRequest request = imageRequests.get(i);
            PostImage image = new PostImage();
            image.setPostId(postId);
            image.setImageUrl(normalizeRequiredText(request.getImageUrl(), "imageUrl is required"));
            image.setImageKey(normalizeOptionalText(request.getImageKey()));
            image.setThumbnailUrl(normalizeOptionalText(request.getThumbnailUrl()));
            image.setWidth(request.getWidth());
            image.setHeight(request.getHeight());
            image.setFileSize(request.getFileSize());
            image.setMimeType(normalizeOptionalText(request.getMimeType()));
            image.setSortOrder(i);
            image.setIsCover(i == 0 ? 1 : 0);
            images.add(image);
        }
        return images;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Convert a list of post entities to response objects with image data.
     * Params:
     * - posts: source post entity list
     * Returns:
     * - List<PostResponse>: response list with images attached
     * Throws: None
     */
    private List<PostResponse> toPostResponses(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, List<PostImageResponse>> imageMap = groupImageResponsesByPostId(postIds);
        return posts.stream()
                .map(post -> toPostResponse(post, imageMap.getOrDefault(post.getId(), List.of())))
                .toList();
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Group image response objects by post id for batch response assembly.
     * Params:
     * - postIds: target post ids
     * Returns:
     * - Map<Long,List<PostImageResponse>>: grouped images keyed by post id
     * Throws: None
     */
    private Map<Long, List<PostImageResponse>> groupImageResponsesByPostId(List<Long> postIds) {
        Map<Long, List<PostImageResponse>> imageMap = new LinkedHashMap<>();
        for (PostImage image : postImageMapper.selectByPostIds(postIds)) {
            imageMap.computeIfAbsent(image.getPostId(), key -> new ArrayList<>())
                    .add(toPostImageResponse(image));
        }
        return imageMap;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Convert post entity to API response object.
     * Params:
     * - post: source post entity
     * Returns:
     * - PostResponse: response payload object
     * Throws: None
     */
    private PostResponse toPostResponse(Post post) {
        return toPostResponse(post, postImageMapper.selectByPostId(post.getId())
                .stream()
                .map(this::toPostImageResponse)
                .toList());
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Convert post entity and prepared image list to API response object.
     * Params:
     * - post: source post entity
     * - images: prepared image response list
     * Returns:
     * - PostResponse: response payload object
     * Throws: None
     */
    private PostResponse toPostResponse(Post post, List<PostImageResponse> images) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setAuthorUsername(post.getAuthorUsername());
        response.setStoreId(post.getStoreId());
        response.setCategoryId(post.getCategoryId());
        response.setTitle(post.getTitle());
        response.setDescription(post.getDescription());
        response.setPriceMin(post.getPriceMin());
        response.setPriceMax(post.getPriceMax());
        response.setCurrency(post.getCurrency());
        response.setLocationText(post.getLocationText());
        response.setStatus(post.getStatus());
        response.setModerationReason(post.getModerationReason());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setFavoriteCount(post.getFavoriteCount());
        response.setCommentCount(post.getCommentCount());
        response.setPublishedAt(post.getPublishedAt());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setImages(images);
        return response;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Convert post image entity to API response object.
     * Params:
     * - image: source image entity
     * Returns:
     * - PostImageResponse: image response payload object
     * Throws: None
     */
    private PostImageResponse toPostImageResponse(PostImage image) {
        PostImageResponse response = new PostImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setImageKey(image.getImageKey());
        response.setThumbnailUrl(image.getThumbnailUrl());
        response.setWidth(image.getWidth());
        response.setHeight(image.getHeight());
        response.setFileSize(image.getFileSize());
        response.setMimeType(image.getMimeType());
        response.setSortOrder(image.getSortOrder());
        response.setIsCover(image.getIsCover());
        return response;
    }
}
