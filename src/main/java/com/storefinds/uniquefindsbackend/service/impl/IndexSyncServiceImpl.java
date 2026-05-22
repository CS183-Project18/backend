package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.client.AISearchClient;
import com.storefinds.uniquefindsbackend.common.PostStatus;
import com.storefinds.uniquefindsbackend.dto.ai.IndexPostData;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.event.PostDeletedEvent;
import com.storefinds.uniquefindsbackend.event.PostSearchContentChangedEvent;
import com.storefinds.uniquefindsbackend.event.PostStatusChangedEvent;
import com.storefinds.uniquefindsbackend.exception.AIServiceException;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.IndexSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Rebuild the full AI search indices from published posts when startup or searchable post lifecycle events require refresh.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class IndexSyncServiceImpl implements IndexSyncService {

    private static final Logger log = LoggerFactory.getLogger(IndexSyncServiceImpl.class);
    private static final long REBUILD_DEBOUNCE_MILLIS = 3000L;

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final AISearchClient aiSearchClient;
    private final AtomicLong lastRebuildScheduledAt = new AtomicLong(0L);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Inject the published-post data sources and AI client used to rebuild search indices.
     * Params:
     * - postMapper: post mapper for published post retrieval
     * - postImageMapper: post image mapper for image URL retrieval
     * - aiSearchClient: AI search integration client
     * Returns: None
     * Throws: None
     */
    public IndexSyncServiceImpl(PostMapper postMapper,
                                PostImageMapper postImageMapper,
                                AISearchClient aiSearchClient) {
        this.postMapper = postMapper;
        this.postImageMapper = postImageMapper;
        this.aiSearchClient = aiSearchClient;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Schedule one asynchronous full rebuild while skipping duplicate rebuild requests inside the debounce window.
     * Params:
     * - reason: readable rebuild trigger reason
     * Returns: None
     * Throws: None
     */
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
    public void onApplicationReady() {
        scheduleRebuild("application startup");
    }

    @EventListener
    public void onPostStatusChanged(PostStatusChangedEvent event) {
        if (isPublishedStatus(event.oldStatus()) || isPublishedStatus(event.newStatus())) {
            scheduleRebuild("post status changed: postId=" + event.postId());
        }
    }

    @EventListener
    public void onPostSearchContentChanged(PostSearchContentChangedEvent event) {
        scheduleRebuild("published post search content changed: postId=" + event.postId());
    }

    @EventListener
    public void onPostDeleted(PostDeletedEvent event) {
        if (isPublishedStatus(event.previousStatus())) {
            scheduleRebuild("published post deleted: postId=" + event.postId());
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Build one fresh full published-post payload and send it to the ai-search index rebuild endpoint.
     * Params:
     * - reason: readable rebuild trigger reason
     * Returns: None
     * Throws: None
     */
    private void rebuildPublishedPostIndex(String reason) {
        List<Post> publishedPosts = postMapper.selectPublishedPosts();
        List<Long> postIds = publishedPosts.stream().map(Post::getId).toList();
        Map<Long, List<String>> imageUrlsByPostId = groupImageUrlsByPostId(postIds);
        List<IndexPostData> payload = publishedPosts.stream()
                .map(post -> new IndexPostData(
                        post.getId(),
                        post.getTitle(),
                        post.getDescription(),
                        imageUrlsByPostId.getOrDefault(post.getId(), List.of())
                ))
                .toList();
        try {
            aiSearchClient.buildIndex(payload);
            log.info("rebuilt ai search index successfully: reason={}, postCount={}", reason, payload.size());
        } catch (AIServiceException ex) {
            log.warn("failed to rebuild ai search index: reason={}", reason, ex);
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Group image URLs by post id so the full rebuild payload can describe both semantic and image-search candidates.
     * Params:
     * - postIds: published post ids
     * Returns:
     * - Map<Long, List<String>>: grouped image URL map
     * Throws: None
     */
    private Map<Long, List<String>> groupImageUrlsByPostId(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<String>> imageUrlsByPostId = new LinkedHashMap<>();
        for (PostImage image : postImageMapper.selectByPostIds(postIds)) {
            imageUrlsByPostId.computeIfAbsent(image.getPostId(), ignored -> new ArrayList<>())
                    .add(image.getImageUrl());
        }
        return imageUrlsByPostId;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-22
     * Purpose: Check whether one post status participates in the searchable published-post index.
     * Params:
     * - status: moderation status value
     * Returns:
     * - boolean: true when the status is published
     * Throws: None
     */
    private boolean isPublishedStatus(String status) {
        return PostStatus.PUBLISHED.equalsIgnoreCase(status);
    }
}
