package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.config.SecurityConfig;
import com.storefinds.uniquefindsbackend.controller.admin.AdminAnalyticsController;
import com.storefinds.uniquefindsbackend.controller.admin.AdminCategoryController;
import com.storefinds.uniquefindsbackend.controller.admin.AdminModerationController;
import com.storefinds.uniquefindsbackend.controller.admin.AdminStoreController;
import com.storefinds.uniquefindsbackend.controller.admin.AdminTagController;
import com.storefinds.uniquefindsbackend.dto.AnalyticsDistributionItemResponse;
import com.storefinds.uniquefindsbackend.dto.CategoryResponse;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.NotificationResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.StoreResponse;
import com.storefinds.uniquefindsbackend.dto.TagResponse;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;
import com.storefinds.uniquefindsbackend.service.AdminAnalyticsService;
import com.storefinds.uniquefindsbackend.service.AdminModerationService;
import com.storefinds.uniquefindsbackend.service.CategoryService;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.exception.GlobalExceptionHandler;
import com.storefinds.uniquefindsbackend.security.JwtAuthenticationFilter;
import com.storefinds.uniquefindsbackend.security.RestAccessDeniedHandler;
import com.storefinds.uniquefindsbackend.security.RestAuthenticationEntryPoint;
import com.storefinds.uniquefindsbackend.service.StoreService;
import com.storefinds.uniquefindsbackend.dto.UnreadNotificationCountResponse;
import com.storefinds.uniquefindsbackend.service.CommentService;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import com.storefinds.uniquefindsbackend.service.PostInteractionService;
import com.storefinds.uniquefindsbackend.service.PostService;
import com.storefinds.uniquefindsbackend.service.TagService;
import com.storefinds.uniquefindsbackend.service.TrendingService;
import com.storefinds.uniquefindsbackend.service.UserProfileService;
import com.storefinds.uniquefindsbackend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PostController.class,
        PublicUserProfileController.class,
        CommentController.class,
        CommentInteractionController.class,
        NotificationController.class,
        CategoryController.class,
        StoreController.class,
        TagController.class,
        DiscoveryController.class,
        AdminModerationController.class,
        AdminCategoryController.class,
        AdminStoreController.class,
        AdminTagController.class,
        AdminAnalyticsController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAccessDeniedHandler.class,
        RestAuthenticationEntryPoint.class,
        JwtUtil.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "app.jwt.secret=NzA2MDI4NjQ2MDI2MzY4NzA4MjM0MTA2NTMxMzgzNjE=",
        "app.jwt.expire-seconds=7200"
})
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-13
 * Purpose: Verify social API security rules, ownership behavior, and key response fields through MockMvc.
 * Params: None
 * Returns: None
 * Throws: None
 */
class SocialApiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostInteractionService postInteractionService;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AdminModerationService adminModerationService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private TrendingService trendingService;

    @MockitoBean
    private AdminAnalyticsService adminAnalyticsService;

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Verify guests can access public social read APIs and receive newly added response fields.
     * Params: None
     * Returns: None
     * Throws: Exception
     */
    void guestCanReadPublicPostProfilePostListAndComments() throws Exception {
        PostResponse post = new PostResponse();
        post.setId(42L);
        post.setTitle("Vintage Lamp");
        post.setStatus("PUBLISHED");
        post.setShareUrl("http://localhost:8080/posts/42");

        UserProfileResponse profile = new UserProfileResponse();
        profile.setUserId(7L);
        profile.setUsername("alice");
        profile.setNickname("Alice");

        PostResponse userPost = new PostResponse();
        userPost.setId(99L);
        userPost.setTitle("Old Camera");
        userPost.setShareUrl("http://localhost:8080/posts/99");

        CommentResponse comment = new CommentResponse();
        comment.setId(3L);
        comment.setPostId(42L);
        comment.setContent("looks good");
        comment.setLikeCount(5L);
        comment.setLikedByCurrentUser(false);
        comment.setPinned(true);

        when(postService.getPostById(null, null, 42L)).thenReturn(Result.success(post));
        when(userProfileService.getPublicProfile("alice")).thenReturn(Result.success(profile));
        when(userProfileService.getPublicPosts("alice", 1, 20)).thenReturn(Result.success(pageOf(userPost)));
        when(commentService.getCommentsByPostId(null, 42L, 1, 20)).thenReturn(Result.success(pageOf(comment)));

        mockMvc.perform(get("/api/posts/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shareUrl").value("http://localhost:8080/posts/42"));

        mockMvc.perform(get("/api/users/alice/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"));

        mockMvc.perform(get("/api/users/alice/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].shareUrl").value("http://localhost:8080/posts/99"));

        mockMvc.perform(get("/api/posts/42/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].likeCount").value(5))
                .andExpect(jsonPath("$.data.items[0].likedByCurrentUser").value(false))
                .andExpect(jsonPath("$.data.items[0].pinned").value(true));
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Verify guests can access newly added structured discovery read APIs.
     * Params: None
     * Returns: None
     * Throws: Exception
     */
    void guestCanReadStructuredDiscoveryApis() throws Exception {
        CategoryResponse category = new CategoryResponse();
        category.setId(1L);
        category.setName("Home Decor");
        category.setActive(true);

        StoreResponse store = new StoreResponse();
        store.setId(2L);
        store.setName("North Lane Gift House");
        store.setStatus("ACTIVE");

        TagResponse tag = new TagResponse();
        tag.setId(3L);
        tag.setName("retro");

        AnalyticsDistributionItemResponse item = new AnalyticsDistributionItemResponse(1L, "Home Decor", 6L);

        when(categoryService.getCategories(true)).thenReturn(Result.success(List.of(category)));
        when(categoryService.getCategoryTree(true)).thenReturn(Result.success(List.of(category)));
        when(storeService.getStores(true)).thenReturn(Result.success(List.of(store)));
        when(storeService.getStoreById(2L, true)).thenReturn(Result.success(store));
        when(tagService.getTags()).thenReturn(Result.success(List.of(tag)));
        when(trendingService.getTrendingCategories(10)).thenReturn(Result.success(List.of(item)));
        when(trendingService.getTrendingTags(10)).thenReturn(Result.success(List.of(item)));
        when(trendingService.getTrendingStores(10)).thenReturn(Result.success(List.of(item)));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Home Decor"));

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Home Decor"));

        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

        mockMvc.perform(get("/api/stores/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("North Lane Gift House"));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("retro"));

        mockMvc.perform(get("/api/discovery/trending/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Home Decor"));

        mockMvc.perform(get("/api/discovery/trending/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].count").value(6));

        mockMvc.perform(get("/api/discovery/trending/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].count").value(6));
    }

    @Test
    void guestCanUsePublicImageSearchEndpoint() throws Exception {
        PostResponse post = new PostResponse();
        post.setId(42L);
        post.setTitle("Vintage Lamp");

        MockMultipartFile file = new MockMultipartFile("file", "lamp.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(postService.searchPublishedPostsByImage(org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.isNull(),
                        org.mockito.ArgumentMatchers.eq(1),
                        org.mockito.ArgumentMatchers.eq(20)))
                .thenReturn(Result.success(pageOf(post)));

        mockMvc.perform(multipart("/api/posts/search/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].id").value(42));
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Verify guests cannot call protected social interaction and notification APIs.
     * Params: None
     * Returns: None
     * Throws: Exception
     */
    void guestCannotLikePinCommentOrReadNotifications() throws Exception {
        mockMvc.perform(post("/api/comments/8/like"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(post("/api/comments/8/pin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("unauthorized"));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"a","description":"b","images":[]}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Verify normal authenticated users can like comments but cannot pin comments without ownership.
     * Params: None
     * Returns: None
     * Throws: Exception
     */
    void authenticatedUserCanLikeCommentButCannotPinOthersComment() throws Exception {
        when(commentService.likeComment(2L, 8L)).thenReturn(Result.success("comment liked", null));
        when(commentService.pinComment(2L, "USER", 8L)).thenThrow(new BusinessException("you cannot pin this comment"));

        mockMvc.perform(post("/api/comments/8/like")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("comment liked"));

        mockMvc.perform(post("/api/comments/8/pin")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("you cannot pin this comment"));
    }

    @Test
    void postOwnerAndAdminCanPinComment() throws Exception {
        when(commentService.pinComment(9L, "USER", 8L)).thenReturn(Result.success("comment pinned", null));
        when(commentService.pinComment(1L, "ADMIN", 8L)).thenReturn(Result.success("comment pinned", null));

        mockMvc.perform(post("/api/comments/8/pin")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(9L, "owner", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("comment pinned"));

        mockMvc.perform(post("/api/comments/8/pin")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(1L, "admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("comment pinned"));
    }

    @Test
    void adminCanViewUnpublishedPostDetail() throws Exception {
        PostResponse pendingPost = new PostResponse();
        pendingPost.setId(77L);
        pendingPost.setStatus("PENDING_REVIEW");
        pendingPost.setShareUrl("http://localhost:8080/posts/77");
        when(postService.getPostById(1L, "ADMIN", 77L)).thenReturn(Result.success(pendingPost));

        mockMvc.perform(get("/api/posts/77")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(1L, "admin", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.data.shareUrl").value("http://localhost:8080/posts/77"));

        verify(postService).getPostById(1L, "ADMIN", 77L);
    }

    @Test
    void notificationEndpointsRespectOwnershipAndAuthentication() throws Exception {
        NotificationResponse notification = new NotificationResponse();
        notification.setId(5L);
        notification.setEventType("COMMENT_LIKED");
        notification.setTargetType("COMMENT");
        notification.setTargetId(8L);
        notification.setRead(false);

        when(notificationService.getMyNotifications(2L, null, 1, 20)).thenReturn(Result.success(pageOf(notification)));
        when(notificationService.getMyNotifications(2L, "COMMENT_LIKED", 1, 20)).thenReturn(Result.success(pageOf(notification)));
        when(notificationService.getUnreadCount(2L)).thenReturn(Result.success(new UnreadNotificationCountResponse(3L)));
        when(notificationService.markAsRead(2L, 5L)).thenReturn(Result.success("notification marked as read", null));
        when(notificationService.markAllAsRead(2L)).thenReturn(Result.success("all notifications marked as read", null));
        when(notificationService.markAsRead(2L, 9L)).thenThrow(new BusinessException("notification not found"));

        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].eventType").value("COMMENT_LIKED"));

        mockMvc.perform(get("/api/notifications")
                        .queryParam("eventType", "COMMENT_LIKED")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].eventType").value("COMMENT_LIKED"));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(3));

        mockMvc.perform(post("/api/notifications/5/read")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("notification marked as read"));

        mockMvc.perform(post("/api/notifications/read-all")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("all notifications marked as read"));

        mockMvc.perform(post("/api/notifications/9/read")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("notification not found"));
    }

    @Test
    void nonAdminCannotAccessModerationEndpoints() throws Exception {
        mockMvc.perform(post("/api/admin/moderation/reports/9/resolve")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-18
     * Purpose: Verify non-admin users remain blocked from structured admin and analytics endpoints.
     * Params: None
     * Returns: None
     * Throws: Exception
     */
    void nonAdminCannotAccessStructuredAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/analytics/overview")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/admin/tags")
                        .header(HttpHeaders.AUTHORIZATION, bearerFor(2L, "bob", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"retro"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private String bearerFor(Long userId, String username, String role) {
        return "Bearer " + jwtUtil.generateToken(userId, username, role);
    }

    private <T> PageResponse<T> pageOf(T item) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setTotal(1L);
        pageResponse.setPage(1);
        pageResponse.setPageSize(20);
        pageResponse.setItems(List.of(item));
        return pageResponse;
    }
}
