package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostImageMapper;
import com.storefinds.uniquefindsbackend.mapper.PostLikeMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.DiscoveryFacade;
import com.storefinds.uniquefindsbackend.service.InteractionEventService;
import com.storefinds.uniquefindsbackend.service.SearchQueryParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private SearchQueryParser searchQueryParser;

    @Mock
    private DiscoveryFacade discoveryFacade;

    @Mock
    private InteractionEventService interactionEventService;

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
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
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
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
                "http://localhost:8080"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.getPostById(null, null, 5L));
        assertEquals("post is not available", ex.getMessage());
    }

    @Test
    void searchPublishedPostsRecordsSearchEventBeforeDelegating() {
        when(searchQueryParser.parsePostSearchQuery("lamp", 8L, "latest", 1, 20))
                .thenReturn(new PostSearchQuery("lamp", "%lamp%", 8L, "latest", 1, 20, 0));

        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                postImageMapper,
                postLikeMapper,
                postFavoriteMapper,
                searchQueryParser,
                discoveryFacade,
                interactionEventService,
                "http://localhost:8080"
        );

        assertThrows(NullPointerException.class, () -> service.searchPublishedPosts(2L, "lamp", 8L, "latest", 1, 20));
        verify(interactionEventService).record(
                org.mockito.ArgumentMatchers.eq("SEARCH_REQUEST"),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.anyMap()
        );
    }
}
