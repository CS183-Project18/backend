package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.config.FileStorageProperties;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.CategorySummaryResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.StoreSummaryResponse;
import com.storefinds.uniquefindsbackend.entity.PostImage;
import com.storefinds.uniquefindsbackend.entity.Tag;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.PostTagMapper;
import com.storefinds.uniquefindsbackend.mapper.TagMapper;
import com.storefinds.uniquefindsbackend.service.CategoryService;
import com.storefinds.uniquefindsbackend.service.DiscoveryFacade;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.SearchQueryParser;
import com.storefinds.uniquefindsbackend.service.StoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostImageMapper postImageMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private PostFavoriteMapper postFavoriteMapper;

    @Mock
    private PostTagMapper postTagMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private SearchQueryParser searchQueryParser;

    @Mock
    private DiscoveryFacade discoveryFacade;

    @Mock
    private InteractionEventService interactionEventService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private StoreService storeService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void updatePostRejectsNonOwner() {
        Post post = new Post();
        post.setId(5L);
        post.setUserId(7L);
        post.setStatus("PUBLISHED");
        when(postMapper.selectById(5L)).thenReturn(post);

        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                postImageMapper,
                postLikeMapper,
                postFavoriteMapper,
                postTagMapper,
                tagMapper,
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
                categoryService,
                storeService,
                fileStorageProperties(),
                applicationEventPublisher,
                "http://localhost:8080"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deletePost(3L, 5L));
        assertEquals("you can only operate your own posts", ex.getMessage());
        verify(postMapper, never()).softDeleteById(5L, 3L);
    }

    @Test
    void guestCannotReadHiddenPostDetail() {
        Post post = new Post();
        post.setId(5L);
        post.setUserId(7L);
        post.setStatus("HIDDEN");
        when(postMapper.selectById(5L)).thenReturn(post);

        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                postImageMapper,
                postLikeMapper,
                postFavoriteMapper,
                postTagMapper,
                tagMapper,
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
                categoryService,
                storeService,
                fileStorageProperties(),
                applicationEventPublisher,
                "http://localhost:8080"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getPostById(null, null, 5L));
        assertEquals("post is not available", ex.getMessage());
    }

    @Test
    void searchPublishedPostsRecordsSearchEventBeforeDelegating() {
        PostSearchQuery query = new PostSearchQuery("lamp", "%lamp%", 8L, 6L, List.of(3L, 4L), null, null, "latest", true, 1, 20, 0);
        when(searchQueryParser.parsePostSearchQuery("lamp", 8L, 6L, List.of(3L, 4L), null, null, "latest", 1, 20))
                .thenReturn(query);
        PageResponse<Post> pageResponse = new PageResponse<>();
        pageResponse.setTotal(0L);
        pageResponse.setPage(1);
        pageResponse.setPageSize(20);
        pageResponse.setItems(List.of());
        when(discoveryFacade.searchPublishedPosts(query)).thenReturn(pageResponse);

        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                postImageMapper,
                postLikeMapper,
                postFavoriteMapper,
                postTagMapper,
                tagMapper,
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
                categoryService,
                storeService,
                fileStorageProperties(),
                applicationEventPublisher,
                "http://localhost:8080"
        );

        service.searchPublishedPosts(2L, "lamp", 8L, 6L, List.of(3L, 4L), null, null, "latest", 1, 20);
        verify(interactionEventService).record(
                org.mockito.ArgumentMatchers.eq("SEARCH_REQUEST"),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.argThat(metadata ->
                        "lamp".equals(metadata.get("keyword"))
                                && Long.valueOf(8L).equals(metadata.get("categoryId"))
                                && Long.valueOf(6L).equals(metadata.get("storeId"))
                                && List.of(3L, 4L).equals(metadata.get("tagIds"))
                                && "latest".equals(metadata.get("sort")))
        );
    }

    @Test
    void getPublishedPostsBuildsStructuredSummaryFields() {
        Post post = new Post();
        post.setId(11L);
        post.setUserId(5L);
        post.setStoreId(7L);
        post.setCategoryId(8L);
        post.setTitle("Retro Lamp");
        post.setDescription("Warm light");
        post.setStatus("PUBLISHED");

        PostImage image = new PostImage();
        image.setId(21L);
        image.setPostId(11L);
        image.setImageUrl("https://example.com/lamp.jpg");
        image.setSortOrder(0);
        image.setIsCover(1);

        Tag tag = new Tag();
        tag.setId(31L);
        tag.setPostId(11L);
        tag.setName("retro");

        StoreSummaryResponse storeSummary = new StoreSummaryResponse();
        storeSummary.setId(7L);
        storeSummary.setName("North Lane Gift House");

        CategorySummaryResponse categorySummary = new CategorySummaryResponse();
        categorySummary.setId(8L);
        categorySummary.setName("Home Decor");

        when(postMapper.countPublishedPosts()).thenReturn(1L);
        when(postMapper.selectPublishedPostsPage(0, 20)).thenReturn(List.of(post));
        when(storeService.getStoreSummaryMap(List.of(7L))).thenReturn(Map.of(7L, storeSummary));
        when(categoryService.getCategorySummaryMap(List.of(8L))).thenReturn(Map.of(8L, categorySummary));
        when(postTagMapper.selectTagsByPostIds(List.of(11L))).thenReturn(List.of(tag));
        when(postImageMapper.selectByPostIds(List.of(11L))).thenReturn(List.of(image));
        when(postLikeMapper.selectLikedPostIds(2L, List.of(11L))).thenReturn(List.of(11L));
        when(postFavoriteMapper.selectFavoritedPostIds(2L, List.of(11L))).thenReturn(List.of(11L));

        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                postImageMapper,
                postLikeMapper,
                postFavoriteMapper,
                postTagMapper,
                tagMapper,
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
                categoryService,
                storeService,
                fileStorageProperties(),
                applicationEventPublisher,
                "http://localhost:8080"
        );

        PostResponse response = service.getPublishedPosts(2L, 1, 20).data().getItems().get(0);

        assertEquals("North Lane Gift House", response.getStoreSummary().getName());
        assertEquals("Home Decor", response.getCategorySummary().getName());
        assertEquals("retro", response.getTags().get(0).getName());
        assertEquals("https://example.com/lamp.jpg", response.getImages().get(0).getImageUrl());
        assertEquals(true, response.getLikedByCurrentUser());
        assertEquals(true, response.getFavoritedByCurrentUser());
    }

    private FileStorageProperties fileStorageProperties() {
        FileStorageProperties fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setAllowedContentTypes(List.of("image/jpeg", "image/png", "image/webp"));
        fileStorageProperties.setMaxImageSize(5L * 1024 * 1024);
        return fileStorageProperties;
    }
}
