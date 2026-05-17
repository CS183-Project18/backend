package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreatePostRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.SharePostResponse;
import com.storefinds.uniquefindsbackend.dto.UpdatePostRequest;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.PostInteractionService;
import com.storefinds.uniquefindsbackend.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostInteractionService postInteractionService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-17
     * Purpose: Inject post service for post CRUD endpoints.
     * Params:
     * - postService: post business service
     * Returns: None
     * Throws: None
     */
    public PostController(PostService postService, PostInteractionService postInteractionService) {
        this.postService = postService;
        this.postInteractionService = postInteractionService;
    }

    @PostMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Create a new post for the current authenticated user.
     * Params:
     * - request: create post payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<PostResponse>: created post detail
     * Throws:
     * - BusinessException: when current request is unauthenticated or payload is invalid
     */
    public Result<PostResponse> createPost(@RequestBody @Valid CreatePostRequest request,
                                           Authentication authentication) {
        return postService.createPost(requireCurrentUser(authentication).userId(), request);
    }

    @GetMapping("/{postId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Query one post detail visible to the current user.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<PostResponse>: matched post detail
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is not accessible
     */
    public Result<PostResponse> getPost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                        Authentication authentication) {
        CurrentUser currentUser = extractCurrentUser(authentication);
        return postService.getPostById(
                currentUser == null ? null : currentUser.userId(),
                currentUser == null ? null : currentUser.role(),
                postId
        );
    }

    @GetMapping("/published")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Query published posts for feed display.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<List<PostResponse>>: published post list
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<PageResponse<PostResponse>> getPublishedPosts(@RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                                @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                                Authentication authentication) {
        return postService.getPublishedPosts(extractCurrentUserId(authentication), page, pageSize);
    }

    @GetMapping("/mine")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Query all non-deleted posts created by the current user.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<List<PostResponse>>: current user's post list
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<PageResponse<PostResponse>> getMyPosts(@RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                         @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                         Authentication authentication) {
        return postService.getMyPosts(requireCurrentUser(authentication).userId(), page, pageSize);
    }

    @GetMapping("/search")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Search published posts by keyword, category, and sort option.
     * Params:
     * - keyword: optional search keyword
     * - categoryId: optional category id
     * - sort: optional sort option
     * - page: target page number starting from 1
     * - pageSize: target page size
     * - authentication: spring authentication object
     * Returns:
     * - Result<PageResponse<PostResponse>>: matched published post page
     * Throws: None
     */
    public Result<PageResponse<PostResponse>> searchPosts(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Long categoryId,
                                                          @RequestParam(defaultValue = "latest") String sort,
                                                          @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                          @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                          Authentication authentication) {
        return postService.searchPublishedPosts(extractCurrentUserId(authentication), keyword, categoryId, sort, page, pageSize);
    }

    @GetMapping("/trending")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Query one page of trending published posts within the selected time window.
     * Params:
     * - window: trending window option: daily, weekly, or monthly
     * - page: target page number starting from 1
     * - pageSize: target page size
     * - authentication: spring authentication object
     * Returns:
     * - Result<PageResponse<PostResponse>>: trending post page
     * Throws: None
     */
    public Result<PageResponse<PostResponse>> getTrendingPosts(@RequestParam(defaultValue = "daily") String window,
                                                               @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                               @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                               Authentication authentication) {
        return postService.getTrendingPosts(extractCurrentUserId(authentication), window, page, pageSize);
    }

    @PutMapping("/{postId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Update one post owned by the current authenticated user.
     * Params:
     * - postId: target post id
     * - request: update post payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<PostResponse>: updated post detail
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is not owned by user
     */
    public Result<PostResponse> updatePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                           @RequestBody @Valid UpdatePostRequest request,
                                           Authentication authentication) {
        return postService.updatePost(requireCurrentUser(authentication).userId(), postId, request);
    }

    @DeleteMapping("/{postId}")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Soft delete one post owned by the current authenticated user.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: deletion result
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is not owned by user
     */
    public Result<Void> deletePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                   Authentication authentication) {
        return postService.deletePost(requireCurrentUser(authentication).userId(), postId);
    }

    @PostMapping("/{postId}/share")
    public Result<SharePostResponse> sharePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId) {
        return postInteractionService.sharePost(postId);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-17
     * Purpose: Extract current authenticated user from spring security context.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - CurrentUser: authenticated principal wrapper
     * Throws:
     * - BusinessException: when request is unauthenticated
     */
    private CurrentUser requireCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return currentUser;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Extract current authenticated user id when request may come from guest.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Long: authenticated user id or null
     * Throws: None
     */
    private Long extractCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return currentUser.userId();
    }

    private CurrentUser extractCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return currentUser;
    }
}
