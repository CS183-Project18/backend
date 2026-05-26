package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.client.AISearchClient;
import com.storefinds.uniquefindsbackend.common.PostStatus;
import com.storefinds.uniquefindsbackend.config.AISearchProperties;
import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexResponse;
import com.storefinds.uniquefindsbackend.dto.ai.IndexPostData;
import com.storefinds.uniquefindsbackend.entity.Category;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.entity.Store;
import com.storefinds.uniquefindsbackend.entity.Tag;
import com.storefinds.uniquefindsbackend.event.PostDeletedEvent;
import com.storefinds.uniquefindsbackend.event.PostSearchContentChangedEvent;
import com.storefinds.uniquefindsbackend.event.PostStatusChangedEvent;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import com.storefinds.uniquefindsbackend.mapper.CategoryMapper;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.PostTagMapper;
import com.storefinds.uniquefindsbackend.mapper.StoreMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IndexSyncServiceImpl implements IndexSyncService {

    private static final Logger log = LoggerFactory.getLogger(IndexSyncServiceImpl.class);
    private static final long REBUILD_DEBOUNCE_MILLIS = 3000L;

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final PostTagMapper postTagMapper;
    private final CategoryMapper categoryMapper;
    private final StoreMapper storeMapper;
    private final AISearchClient aiSearchClient;
    private final AISearchProperties aiSearchProperties;
    private final AtomicLong lastRebuildScheduledAt = new AtomicLong(0L);

    public IndexSyncServiceImpl(PostMapper postMapper,
                                PostImageMapper postImageMapper,
                                PostTagMapper postTagMapper,
                                CategoryMapper categoryMapper,
                                StoreMapper storeMapper,
                                AISearchClient aiSearchClient,
                                AISearchProperties aiSearchProperties) {
        this.postMapper = postMapper;
        this.postImageMapper = postImageMapper;
        this.postTagMapper = postTagMapper;
        this.categoryMapper = categoryMapper;
        this.storeMapper = storeMapper;
        this.aiSearchClient = aiSearchClient;
        this.aiSearchProperties = aiSearchProperties;
    }

    @Override
    @Async
    public void scheduleRebuild(String reason) {
        long now = System.currentTimeMillis();
        long lastScheduledAt = lastRebuildScheduledAt.get();
        if (now - lastScheduledAt < REBUILD_DEBOUNCE_MILLIS) {
            log.info("skip ai index rebuild because it is already scheduled recently: reason={}", reason);
            return;
        }
        lastRebuildScheduledAt.set(now);
        rebuildPublishedPostIndex(reason);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onApplicationReady() {
        scheduleRebuild("application startup");
    }

    @EventListener
    @Async
    public void onPostStatusChanged(PostStatusChangedEvent event) {
        if (isPublishedStatus(event.oldStatus()) || isPublishedStatus(event.newStatus())) {
            scheduleRebuild("post status changed: postId=" + event.postId());
        }
    }

    @EventListener
    @Async
    public void onPostSearchContentChanged(PostSearchContentChangedEvent event) {
        scheduleRebuild("published post search content changed: postId=" + event.postId());
    }

    @EventListener
    @Async
    public void onPostDeleted(PostDeletedEvent event) {
        if (isPublishedStatus(event.previousStatus())) {
            scheduleRebuild("published post deleted: postId=" + event.postId());
        }
    }

    private void rebuildPublishedPostIndex(String reason) {
        List<Post> publishedPosts = postMapper.selectPublishedPosts();
        List<Long> postIds = publishedPosts.stream().map(Post::getId).toList();
        Map<Long, List<String>> imageUrlsByPostId = groupImageUrlsByPostId(postIds);
        Map<Long, List<String>> tagNamesByPostId = groupTagNamesByPostId(postIds);
        Map<Long, String> categoryNamesById = groupCategoryNamesById(publishedPosts);
        Map<Long, String> storeNamesById = groupStoreNamesById(publishedPosts);
        List<IndexPostData> payload = publishedPosts.stream()
                .map(post -> new IndexPostData(
                        post.getId(),
                        post.getTitle(),
                        post.getDescription(),
                        imageUrlsByPostId.getOrDefault(post.getId(), List.of()),
                        tagNamesByPostId.getOrDefault(post.getId(), List.of()),
                        categoryNamesById.get(post.getCategoryId()),
                        storeNamesById.get(post.getStoreId()),
                        post.getLocationText()
                ))
                .toList();
        try {
            BuildIndexResponse response = aiSearchClient.buildIndex(payload);
            if (response.getData() == null) {
                log.info("rebuilt ai search index successfully: reason={}, postCount={}", reason, payload.size());
                return;
            }
            log.info(
                    "rebuilt ai search index successfully: reason={}, postCount={}, semanticCount={}, imageCount={}, failedImageCount={}",
                    reason,
                    payload.size(),
                    response.getData().getSemanticCount(),
                    response.getData().getImageCount(),
                    response.getData().getFailedImageCount()
            );
        } catch (AIServiceException ex) {
            log.warn("failed to rebuild ai search index: reason={}", reason, ex);
        }
    }

    private Map<Long, List<String>> groupImageUrlsByPostId(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> imageUrlsByPostId = new LinkedHashMap<>();
        for (PostImage image : postImageMapper.selectByPostIds(postIds)) {
            imageUrlsByPostId.computeIfAbsent(image.getPostId(), ignored -> new ArrayList<>())
                    .add(toAiAccessibleImageUrl(image.getImageUrl()));
        }
        return imageUrlsByPostId;
    }

    private Map<Long, List<String>> groupTagNamesByPostId(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> tagNamesByPostId = new LinkedHashMap<>();
        for (Tag tag : postTagMapper.selectTagsByPostIds(postIds)) {
            if (tag.getPostId() == null || tag.getName() == null || tag.getName().isBlank()) {
                continue;
            }
            tagNamesByPostId.computeIfAbsent(tag.getPostId(), ignored -> new ArrayList<>()).add(tag.getName());
        }
        return tagNamesByPostId;
    }

    private Map<Long, String> groupCategoryNamesById(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> names = new LinkedHashMap<>();
        for (Post post : posts) {
            Long categoryId = post.getCategoryId();
            if (categoryId == null || names.containsKey(categoryId)) {
                continue;
            }
            Category category = categoryMapper.selectById(categoryId);
            if (category != null && category.getName() != null && !category.getName().isBlank()) {
                names.put(categoryId, category.getName());
            }
        }
        return names;
    }

    private Map<Long, String> groupStoreNamesById(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> names = new LinkedHashMap<>();
        for (Post post : posts) {
            Long storeId = post.getStoreId();
            if (storeId == null || names.containsKey(storeId)) {
                continue;
            }
            Store store = storeMapper.selectById(storeId);
            if (store != null) {
                String displayName = buildStoreDisplayName(store);
                if (!displayName.isBlank()) {
                    names.put(storeId, displayName);
                }
            }
        }
        return names;
    }

    private String buildStoreDisplayName(Store store) {
        List<String> parts = new ArrayList<>();
        if (store.getName() != null && !store.getName().isBlank()) {
            parts.add(store.getName().trim());
        }
        if (store.getBranchName() != null && !store.getBranchName().isBlank()) {
            parts.add(store.getBranchName().trim());
        }
        if (store.getCity() != null && !store.getCity().isBlank()) {
            parts.add(store.getCity().trim());
        }
        if (store.getDistrict() != null && !store.getDistrict().isBlank()) {
            parts.add(store.getDistrict().trim());
        }
        return String.join(" ", parts);
    }

    private String toAiAccessibleImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }
        String assetBaseUrl = aiSearchProperties.getAssetBaseUrl();
        if (assetBaseUrl == null || assetBaseUrl.isBlank()) {
            return imageUrl;
        }
        try {
            URI imageUri = URI.create(imageUrl);
            String path = imageUri.getPath();
            if (path == null || !path.startsWith("/uploads/images/")) {
                return imageUrl;
            }
            String normalizedBase = assetBaseUrl.endsWith("/")
                    ? assetBaseUrl.substring(0, assetBaseUrl.length() - 1)
                    : assetBaseUrl;
            StringBuilder rebuiltUrl = new StringBuilder(normalizedBase).append(path);
            if (imageUri.getQuery() != null && !imageUri.getQuery().isBlank()) {
                rebuiltUrl.append('?').append(imageUri.getQuery());
            }
            return rebuiltUrl.toString();
        } catch (IllegalArgumentException ex) {
            log.debug("skip ai image url rewrite because image url is invalid: url={}", imageUrl, ex);
            return imageUrl;
        }
    }

    private boolean isPublishedStatus(String status) {
        return PostStatus.PUBLISHED.equalsIgnoreCase(status);
    }
}
