package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.SharePostResponse;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import com.storefinds.uniquefindsbackend.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-13
 * Purpose: Verify post interaction service behavior around share URL generation and notification idempotency.
 * Params: None
 * Returns: None
 * Throws: None
 */
class PostInteractionServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private PostFavoriteMapper postFavoriteMapper;

    @Mock
    private PostService postService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InteractionEventService interactionEventService;

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Verify repeated likes only create one notification when only one relation insert succeeds.
     * Params: None
     * Returns: None
     * Throws: None
     */
    void likePostCreatesNotificationOnlyWhenRelationInserted() {
        Post post = new Post();
        post.setId(6L);
        post.setUserId(3L);
        post.setStatus("PUBLISHED");
        when(postMapper.selectById(6L)).thenReturn(post);

        PostInteractionServiceImpl service = new PostInteractionServiceImpl(
                postMapper,
                postLikeMapper,
                postFavoriteMapper,
                postService,
                notificationService,
                interactionEventService,
                "https://example.com"
        );

        when(postLikeMapper.insertIgnore(8L, 6L)).thenReturn(0).thenReturn(1);

        service.likePost(8L, 6L);
        service.likePost(8L, 6L);

        verify(notificationService, times(1)).createNotification(3L, 8L, "POST_LIKED", "POST", 6L, 6L);
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Verify share API response builds a normalized canonical post URL.
     * Params: None
     * Returns: None
     * Throws: None
     */
    void sharePostBuildsCanonicalUrl() {
        Post post = new Post();
        post.setId(12L);
        post.setStatus("PUBLISHED");
        when(postMapper.selectById(12L)).thenReturn(post);

        PostInteractionServiceImpl service = new PostInteractionServiceImpl(
                postMapper,
                postLikeMapper,
                postFavoriteMapper,
                postService,
                notificationService,
                interactionEventService,
                "https://example.com/app/"
        );

        SharePostResponse response = service.sharePost(12L).data();

        assertEquals(12L, response.getPostId());
        assertEquals("https://example.com/app/posts/12", response.getShareUrl());
    }

    @Test
    void favoritePostCreatesNotificationOnlyWhenRelationInserted() {
        Post post = new Post();
        post.setId(16L);
        post.setUserId(4L);
        post.setStatus("PUBLISHED");
        when(postMapper.selectById(16L)).thenReturn(post);

        PostInteractionServiceImpl service = new PostInteractionServiceImpl(
                postMapper,
                postLikeMapper,
                postFavoriteMapper,
                postService,
                notificationService,
                interactionEventService,
                "https://example.com"
        );

        when(postFavoriteMapper.insertIgnore(8L, 16L)).thenReturn(1).thenReturn(0);

        service.favoritePost(8L, 16L);
        service.favoritePost(8L, 16L);

        verify(notificationService, times(1)).createNotification(4L, 8L, "POST_FAVORITED", "POST", 16L, 16L);
    }
}
