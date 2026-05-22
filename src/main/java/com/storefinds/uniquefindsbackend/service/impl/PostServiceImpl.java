package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.common.InteractionEventType;
import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.PostStatus;
import com.storefinds.uniquefindsbackend.common.ReportTargetType;
import com.storefinds.uniquefindsbackend.config.FileStorageProperties;
import com.storefinds.uniquefindsbackend.dto.CreatePostRequest;
import com.storefinds.uniquefindsbackend.dto.CategorySummaryResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.PostImageRequest;
import com.storefinds.uniquefindsbackend.dto.PostImageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.StoreSummaryResponse;
import com.storefinds.uniquefindsbackend.dto.TagResponse;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.dto.UpdatePostRequest;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.entity.Tag;
import com.storefinds.uniquefindsbackend.event.PostDeletedEvent;
import com.storefinds.uniquefindsbackend.event.PostSearchContentChangedEvent;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.PostTagMapper;
import com.storefinds.uniquefindsbackend.service.DiscoveryFacade;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.PostService;
import com.storefinds.uniquefindsbackend.service.CategoryService;
import com.storefinds.uniquefindsbackend.service.SearchQueryParser;
import com.storefinds.uniquefindsbackend.service.StoreService;
import com.storefinds.uniquefindsbackend.mapper.TagMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostTagMapper postTagMapper;
    private final TagMapper tagMapper;
    private final SearchQueryParser searchQueryParser;
    private final DiscoveryFacade discoveryFacade;
    private final InteractionEventService interactionEventService;
    private final CategoryService categoryService;
    private final StoreService storeService;
    private final FileStorageProperties fileStorageProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String publicBaseUrl;
    private static final int MAX_TAGS_PER_POST = 5;

    public PostServiceImpl(PostMapper postMapper,
                           PostImageMapper postImageMapper,
                           PostLikeMapper postLikeMapper,
                           PostFavoriteMapper postFavoriteMapper,
                           PostTagMapper postTagMapper,
                           TagMapper tagMapper,
                           SearchQueryParser searchQueryParser,
                           DiscoveryFacade discoveryFacade,
                           InteractionEventService interactionEventService,
                           CategoryService categoryService,
                           StoreService storeService,
                           FileStorageProperties fileStorageProperties,
                           ApplicationEventPublisher applicationEventPublisher,
                           @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.postMapper = postMapper;
        this.postImageMapper = postImageMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.postTagMapper = postTagMapper;
        this.tagMapper = tagMapper;
        this.searchQueryParser = searchQueryParser;
        this.discoveryFacade = discoveryFacade;
        this.interactionEventService = interactionEventService;
        this.categoryService = categoryService;
        this.storeService = storeService;
        this.fileStorageProperties = fileStorageProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    @Transactional
    public Result<PostResponse> createPost(Long userId, CreatePostRequest request) {
        validatePriceRange(request.getPriceMin(), request.getPriceMax());
        categoryService.validateCategorySelectable(request.getCategoryId());
        storeService.validateStoreSelectable(request.getStoreId());
        List<Long> tagIds = normalizeAndValidateTagIds(request.getTagIds());

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
        post.setStatus(PostStatus.PENDING_REVIEW);
        postMapper.insert(post);
        replacePostImages(post.getId(), request.getImages());
        replacePostTags(post.getId(), tagIds);
        interactionEventService.record(
                InteractionEventType.POST_CREATE,
                userId,
                post.getId(),
                null,
                ReportTargetType.POST,
                post.getId(),
                buildMetadata(
                        "categoryId", request.getCategoryId(),
                        "storeId", request.getStoreId(),
                        "tagIds", tagIds,
                        "status", post.getStatus()
                )
        );

        Post createdPost = postMapper.selectById(post.getId());
        return Result.success("post created", buildPostResponseForUser(userId, createdPost));
    }

    @Override
    public Result<PostResponse> getPostById(Long userId, String userRole, Long postId) {
        Post post = requireAccessiblePost(userId, userRole, postId);
        if (PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            postMapper.increaseViewCount(postId);
            interactionEventService.record(
                    InteractionEventType.POST_VIEW,
                    userId,
                    postId,
                    null,
                    ReportTargetType.POST,
                postId,
                buildMetadata("viewerRole", userRole == null ? "GUEST" : userRole)
            );
            post = postMapper.selectById(postId);
        }
        return Result.success(buildPostResponseForUser(userId, post));
    }

    @Override
    public Result<PageResponse<PostResponse>> getPublishedPosts(Long userId, int page, int pageSize) {
        return Result.success(buildPostPage(
                postMapper.countPublishedPosts(),
                page,
                pageSize,
                postMapper.selectPublishedPostsPage(toOffset(page, pageSize), pageSize),
                userId
        ));
    }

    @Override
    public Result<PageResponse<PostResponse>> getMyPosts(Long userId, int page, int pageSize) {
        return Result.success(buildPostPage(
                postMapper.countByUserId(userId),
                page,
                pageSize,
                postMapper.selectByUserIdPage(userId, toOffset(page, pageSize), pageSize),
                userId
        ));
    }

    @Override
    public Result<PageResponse<PostResponse>> searchPublishedPosts(Long userId,
                                                                   String keyword,
                                                                   Long categoryId,
                                                                   Long storeId,
                                                                   List<Long> tagIds,
                                                                   BigDecimal priceMin,
                                                                   BigDecimal priceMax,
                                                                   String sort,
                                                                   int page,
                                                                   int pageSize) {
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery(keyword, categoryId, storeId, tagIds, priceMin, priceMax, sort, page, pageSize);
        interactionEventService.record(
                InteractionEventType.SEARCH_REQUEST,
                userId,
                null,
                null,
                null,
                null,
                buildMetadata(
                        "keyword", query.keyword(),
                        "categoryId", query.categoryId(),
                        "storeId", query.storeId(),
                        "tagIds", query.tagIds(),
                        "priceMin", query.priceMin(),
                        "priceMax", query.priceMax(),
                        "sort", query.sort(),
                        "page", query.page(),
                        "pageSize", query.pageSize()
                )
        );
        PageResponse<Post> postPage = discoveryFacade.searchPublishedPosts(query);
        return Result.success(buildPostPage(postPage.getTotal(), postPage.getPage(), postPage.getPageSize(), postPage.getItems(), userId));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Search published posts by one uploaded image and gracefully degrade when AI image search is unavailable.
     * Params:
     * - userId: current authenticated user id or null
     * - file: uploaded image file
     * - categoryId: optional category id
     * - storeId: optional store id
     * - tagIds: optional tag id list
     * - priceMin: optional minimum price filter
     * - priceMax: optional maximum price filter
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - Result<PageResponse<PostResponse>>: matched published post page or one empty degraded page
     * Throws: None
     */
    public Result<PageResponse<PostResponse>> searchPublishedPostsByImage(Long userId,
                                                                          MultipartFile file,
                                                                          Long categoryId,
                                                                          Long storeId,
                                                                          List<Long> tagIds,
                                                                          BigDecimal priceMin,
                                                                          BigDecimal priceMax,
                                                                          int page,
                                                                          int pageSize) {
        validateSearchImageFile(file);
        PostSearchQuery query = searchQueryParser.parsePostSearchQuery(null, categoryId, storeId, tagIds, priceMin, priceMax, null, page, pageSize);
        try {
            PageResponse<Post> postPage = discoveryFacade.searchPublishedPostsByImage(query, file);
            return Result.success(buildPostPage(postPage.getTotal(), postPage.getPage(), postPage.getPageSize(), postPage.getItems(), userId));
        } catch (RuntimeException ex) {
            if (!(ex instanceof AIServiceException)) {
                throw ex;
            }
            return Result.success("image search is temporarily unavailable", buildPostPage(0L, page, pageSize, List.of(), userId));
        }
    }

    @Override
    public Result<PageResponse<PostResponse>> getTrendingPosts(Long userId,
                                                               String window,
                                                               int page,
                                                               int pageSize) {
        TrendingPostsQuery query = searchQueryParser.parseTrendingPostsQuery(window, page, pageSize);
        PageResponse<Post> postPage = discoveryFacade.getTrendingPosts(query);
        return Result.success(buildPostPage(postPage.getTotal(), postPage.getPage(), postPage.getPageSize(), postPage.getItems(), userId));
    }

    @Override
    @Transactional
    public Result<PostResponse> updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post existingPost = requireOwnedPost(userId, postId);
        validatePriceRange(request.getPriceMin(), request.getPriceMax());
        categoryService.validateCategorySelectable(request.getCategoryId());
        storeService.validateStoreSelectable(request.getStoreId());
        List<Long> tagIds = normalizeAndValidateTagIds(request.getTagIds());

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
        replacePostTags(postId, tagIds);

        if (PostStatus.PUBLISHED.equalsIgnoreCase(existingPost.getStatus())) {
            applicationEventPublisher.publishEvent(new PostSearchContentChangedEvent(postId));
        }

        Post updatedPost = postMapper.selectById(postId);
        return Result.success("post updated", buildPostResponseForUser(userId, updatedPost));
    }

    @Override
    @Transactional
    public Result<Void> deletePost(Long userId, Long postId) {
        Post post = requireOwnedPost(userId, postId);
        postMapper.softDeleteById(postId, userId);
        if (PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            applicationEventPublisher.publishEvent(new PostDeletedEvent(postId, post.getStatus()));
        }
        return Result.success("post deleted", null);
    }

    private Post requireOwnedPost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || PostStatus.DELETED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "post not found");
        }
        if (!userId.equals(post.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "you can only operate your own posts");
        }
        return post;
    }

    private Post requireAccessiblePost(Long userId, String userRole, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || PostStatus.DELETED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "post not found");
        }
        boolean isOwner = userId != null && userId.equals(post.getUserId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isPublished = PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus());
        if (!isOwner && !isAdmin && !isPublished) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post is not available");
        }
        return post;
    }

    private void validatePriceRange(BigDecimal priceMin, BigDecimal priceMax) {
        if (priceMin != null && priceMax != null && priceMin.compareTo(priceMax) > 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "priceMin cannot be greater than priceMax");
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Validate one uploaded image file for AI image-search usage with the supported size and MIME constraints.
     * Params:
     * - file: uploaded image file
     * Returns: None
     * Throws:
     * - BusinessException: when the uploaded file is empty, oversized, or not one supported image type
     */
    private void validateSearchImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "image file is required");
        }
        if (file.getSize() > fileStorageProperties.getMaxImageSize()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "image file is too large");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "only jpeg, png, and webp images are supported");
        }
        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (!List.of("image/jpeg", "image/png", "image/webp").contains(normalizedContentType)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "only jpeg, png, and webp images are supported");
        }
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, errorMessage);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeCurrency(String currency) {
        String normalized = normalizeOptionalText(currency);
        return normalized == null ? "CNY" : normalized.toUpperCase();
    }

    private List<Long> normalizeAndValidateTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<Long> normalized = tagIds.stream()
                .filter(tagId -> tagId != null && tagId > 0)
                .distinct()
                .toList();
        if (normalized.size() > MAX_TAGS_PER_POST) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "a post can contain at most " + MAX_TAGS_PER_POST + " tags");
        }
        List<Tag> tags = tagMapper.selectByIds(normalized);
        if (tags.size() != normalized.size()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "some tags do not exist");
        }
        return normalized;
    }

    private void replacePostImages(Long postId, List<PostImageRequest> imageRequests) {
        postImageMapper.deleteByPostId(postId);
        List<PostImage> images = buildPostImages(postId, imageRequests);
        if (!images.isEmpty()) {
            postImageMapper.batchInsert(images);
        }
    }

    private void replacePostTags(Long postId, List<Long> tagIds) {
        postTagMapper.deleteByPostId(postId);
        if (tagIds != null && !tagIds.isEmpty()) {
            postTagMapper.batchInsert(postId, tagIds);
        }
    }

    private List<PostImage> buildPostImages(Long postId, List<PostImageRequest> imageRequests) {
        if (imageRequests == null || imageRequests.isEmpty()) {
            return Collections.emptyList();
        }
        if (imageRequests.size() > 9) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "a post can contain at most 9 images");
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

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    private PageResponse<PostResponse> buildPostPage(long total,
                                                     int page,
                                                     int pageSize,
                                                     List<Post> posts,
                                                     Long userId) {
        PageResponse<PostResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(buildPostResponsesForUser(userId, posts));
        return response;
    }

    public List<PostResponse> buildPostResponsesForUser(Long userId, List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, StoreSummaryResponse> storeSummaryMap = storeService.getStoreSummaryMap(posts.stream().map(Post::getStoreId).toList());
        Map<Long, CategorySummaryResponse> categorySummaryMap = categoryService.getCategorySummaryMap(posts.stream().map(Post::getCategoryId).toList());
        Map<Long, List<TagResponse>> tagMap = getTagResponseMap(postIds);
        Map<Long, List<PostImageResponse>> imageMap = groupImageResponsesByPostId(postIds);
        Set<Long> likedPostIds = userId == null
                ? Set.of()
                : new LinkedHashSet<>(postLikeMapper.selectLikedPostIds(userId, postIds));
        Set<Long> favoritedPostIds = userId == null
                ? Set.of()
                : new LinkedHashSet<>(postFavoriteMapper.selectFavoritedPostIds(userId, postIds));
        return posts.stream()
                .map(post -> toPostResponse(
                        post,
                        imageMap.getOrDefault(post.getId(), List.of()),
                        tagMap.getOrDefault(post.getId(), List.of()),
                        storeSummaryMap.get(post.getStoreId()),
                        categorySummaryMap.get(post.getCategoryId()),
                        likedPostIds.contains(post.getId()),
                        favoritedPostIds.contains(post.getId())
                ))
                .toList();
    }

    private Map<Long, List<PostImageResponse>> groupImageResponsesByPostId(List<Long> postIds) {
        Map<Long, List<PostImageResponse>> imageMap = new LinkedHashMap<>();
        for (PostImage image : postImageMapper.selectByPostIds(postIds)) {
            imageMap.computeIfAbsent(image.getPostId(), key -> new ArrayList<>())
                    .add(toPostImageResponse(image));
        }
        return imageMap;
    }

    private PostResponse buildPostResponseForUser(Long userId, Post post) {
        boolean likedByCurrentUser = userId != null && postLikeMapper.countByUserIdAndPostId(userId, post.getId()) > 0;
        boolean favoritedByCurrentUser = userId != null
                && postFavoriteMapper.countByUserIdAndPostId(userId, post.getId()) > 0;
        return toPostResponse(post, postImageMapper.selectByPostId(post.getId())
                .stream()
                .map(this::toPostImageResponse)
                .toList(),
                postTagMapper.selectTagsByPostId(post.getId()).stream().map(this::toTagResponse).toList(),
                post.getStoreId() == null ? null : storeService.getStoreSummaryMap(List.of(post.getStoreId())).get(post.getStoreId()),
                post.getCategoryId() == null ? null : categoryService.getCategorySummaryMap(List.of(post.getCategoryId())).get(post.getCategoryId()),
                likedByCurrentUser,
                favoritedByCurrentUser);
    }

    private PostResponse toPostResponse(Post post,
                                        List<PostImageResponse> images,
                                        List<TagResponse> tags,
                                        StoreSummaryResponse storeSummary,
                                        CategorySummaryResponse categorySummary,
                                        boolean likedByCurrentUser,
                                        boolean favoritedByCurrentUser) {
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
        response.setStoreSummary(storeSummary);
        response.setCategorySummary(categorySummary);
        response.setTags(tags);
        response.setStatus(post.getStatus());
        response.setModerationReason(post.getModerationReason());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setFavoriteCount(post.getFavoriteCount());
        response.setCommentCount(post.getCommentCount());
        response.setLikedByCurrentUser(likedByCurrentUser);
        response.setFavoritedByCurrentUser(favoritedByCurrentUser);
        response.setShareUrl(buildShareUrl(post.getId()));
        response.setPublishedAt(post.getPublishedAt());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setImages(images);
        return response;
    }

    @Override
    public Map<Long, List<TagResponse>> getTagResponseMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<TagResponse>> tagMap = new LinkedHashMap<>();
        for (Tag tag : postTagMapper.selectTagsByPostIds(postIds)) {
            tagMap.computeIfAbsent(tag.getPostId(), key -> new ArrayList<>()).add(toTagResponse(tag));
        }
        return tagMap;
    }

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

    private TagResponse toTagResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setHeatScore(tag.getHeatScore());
        response.setCreatedAt(tag.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public void likePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !PostStatus.PUBLISHED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "post is not available");
        }
        if (postLikeMapper.countByUserIdAndPostId(userId, postId) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "already liked");
        }
        postLikeMapper.insertIgnore(userId, postId);
    }

    @Override
    @Transactional
    public void unlikePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || PostStatus.DELETED.equalsIgnoreCase(post.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "post not found");
        }
        if (postLikeMapper.countByUserIdAndPostId(userId, postId) == 0) {
            throw new BusinessException(ErrorCode.INVALID_STATE, "not liked");
        }
        postLikeMapper.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public List<Long> getLikedPostIds(Long userId, List<Long> postIds) {
        if (userId == null || postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        return postLikeMapper.selectLikedPostIds(userId, postIds);
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null || postId == null) {
            return false;
        }
        return postLikeMapper.countByUserIdAndPostId(userId, postId) > 0;
    }

    private String buildShareUrl(Long postId) {
        return publicBaseUrl.replaceAll("/+$", "") + "/posts/" + postId;
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
