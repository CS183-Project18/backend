package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.client.AISearchClient;
import com.storefinds.uniquefindsbackend.config.AISearchProperties;
import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexData;
import com.storefinds.uniquefindsbackend.dto.ai.BuildIndexResponse;
import com.storefinds.uniquefindsbackend.entity.Category;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.entity.Store;
import com.storefinds.uniquefindsbackend.entity.Tag;
import com.storefinds.uniquefindsbackend.event.PostStatusChangedEvent;
import com.storefinds.uniquefindsbackend.mapper.CategoryMapper;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.PostTagMapper;
import com.storefinds.uniquefindsbackend.mapper.StoreMapper;
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

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Validate AI search index synchronization behavior.
 */
@ExtendWith(MockitoExtension.class)
class IndexSyncServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostImageMapper postImageMapper;

    @Mock
    private PostTagMapper postTagMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private AISearchClient aiSearchClient;

    @Test
    void scheduleRebuildBuildsPayloadWithImageUrlsAndMetadata() throws Exception {
        Post post = new Post();
        post.setId(7L);
        post.setTitle("Vintage Lamp");
        post.setDescription("Warm desk light");
        post.setCategoryId(3L);
        post.setStoreId(9L);
        post.setLocationText("Portland");

        PostImage image = new PostImage();
        image.setPostId(7L);
        image.setImageUrl("http://localhost:8080/uploads/images/lamp.jpg");

        Tag tag = new Tag();
        tag.setPostId(7L);
        tag.setName("desk-lamp");

        Category category = new Category();
        category.setId(3L);
        category.setName("Lighting");

        Store store = new Store();
        store.setId(9L);
        store.setName("Woodland Mercantile");
        store.setBranchName("Main");
        store.setCity("Portland");

        AISearchProperties properties = new AISearchProperties();
        properties.setAssetBaseUrl("http://backend:8080");

        when(postMapper.selectPublishedPosts()).thenReturn(List.of(post));
        when(postImageMapper.selectByPostIds(List.of(7L))).thenReturn(List.of(image));
        when(postTagMapper.selectTagsByPostIds(List.of(7L))).thenReturn(List.of(tag));
        when(categoryMapper.selectById(3L)).thenReturn(category);
        when(storeMapper.selectById(9L)).thenReturn(store);
        when(aiSearchClient.buildIndex(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(new BuildIndexResponse(200, new BuildIndexData(true, "ok", 1, 1, 1, 0), "success"));

        IndexSyncServiceImpl service = new IndexSyncServiceImpl(
                postMapper,
                postImageMapper,
                postTagMapper,
                categoryMapper,
                storeMapper,
                aiSearchClient,
                properties
        );
        service.scheduleRebuild("test");

        ArgumentCaptor<List> payloadCaptor = ArgumentCaptor.forClass(List.class);
        verify(aiSearchClient).buildIndex(payloadCaptor.capture());
        com.storefinds.uniquefindsbackend.dto.ai.IndexPostData firstPayload =
                (com.storefinds.uniquefindsbackend.dto.ai.IndexPostData) payloadCaptor.getValue().get(0);
        assertEquals("Vintage Lamp", firstPayload.getTitle());
        assertEquals(List.of("http://backend:8080/uploads/images/lamp.jpg"), firstPayload.getImageUrls());
        assertEquals(List.of("desk-lamp"), firstPayload.getTags());
        assertEquals("Lighting", firstPayload.getCategoryName());
        assertEquals("Woodland Mercantile Main Portland", firstPayload.getStoreName());
        assertEquals("Portland", firstPayload.getLocationText());
    }

    @Test
    void nonPublishedStatusChangeDoesNotTriggerRebuild() {
        AISearchProperties properties = new AISearchProperties();
        IndexSyncServiceImpl service = new IndexSyncServiceImpl(
                postMapper,
                postImageMapper,
                postTagMapper,
                categoryMapper,
                storeMapper,
                aiSearchClient,
                properties
        );

        service.onPostStatusChanged(new PostStatusChangedEvent(5L, "PENDING_REVIEW", "REJECTED"));

        verify(aiSearchClient, never()).buildIndex(org.mockito.ArgumentMatchers.anyList());
    }
}
