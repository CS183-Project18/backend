package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.client.AISearchClient;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.event.PostStatusChangedEvent;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexSyncServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostImageMapper postImageMapper;

    @Mock
    private AISearchClient aiSearchClient;

    @Test
    void scheduleRebuildBuildsPayloadWithImageUrls() {
        Post post = new Post();
        post.setId(7L);
        post.setTitle("Vintage Lamp");
        post.setDescription("Warm desk light");

        PostImage image = new PostImage();
        image.setPostId(7L);
        image.setImageUrl("https://example.com/lamp.jpg");

        when(postMapper.selectPublishedPosts()).thenReturn(List.of(post));
        when(postImageMapper.selectByPostIds(List.of(7L))).thenReturn(List.of(image));

        IndexSyncServiceImpl service = new IndexSyncServiceImpl(postMapper, postImageMapper, aiSearchClient);
        service.scheduleRebuild("test");

        ArgumentCaptor<List> payloadCaptor = ArgumentCaptor.forClass(List.class);
        verify(aiSearchClient).buildIndex(payloadCaptor.capture());
        Object firstPayload = payloadCaptor.getValue().get(0);
        assertEquals("Vintage Lamp", ((com.storefinds.uniquefindsbackend.dto.ai.IndexPostData) firstPayload).getTitle());
        assertEquals(List.of("https://example.com/lamp.jpg"),
                ((com.storefinds.uniquefindsbackend.dto.ai.IndexPostData) firstPayload).getImageUrls());
    }

    @Test
    void nonPublishedStatusChangeDoesNotTriggerRebuild() {
        IndexSyncServiceImpl service = new IndexSyncServiceImpl(postMapper, postImageMapper, aiSearchClient);

        service.onPostStatusChanged(new PostStatusChangedEvent(5L, "PENDING_REVIEW", "REJECTED"));

        verify(aiSearchClient, never()).buildIndex(org.mockito.ArgumentMatchers.anyList());
    }
}
