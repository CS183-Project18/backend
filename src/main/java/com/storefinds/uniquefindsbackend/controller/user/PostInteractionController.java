package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.InteractionStatusResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.PostInteractionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/posts")
public class PostInteractionController {

    private final PostInteractionService postInteractionService;

    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Inject post interaction service for like and favorite endpoints.
     * Params:
     * - postInteractionService: post interaction business service
     * Returns: None
     * Throws: None
     */
    public PostInteractionController(PostInteractionService postInteractionService) {
        this.postInteractionService = postInteractionService;
    }

    @PostMapping("/{postId}/like")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Like one published post for the current authenticated user.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is unavailable
     */
    public Result<Void> likePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                 Authentication authentication) {
        return postInteractionService.likePost(requireCurrentUser(authentication).userId(), postId);
    }

    @DeleteMapping("/{postId}/like")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Cancel one like relation on the specified post.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is unavailable
     */
    public Result<Void> unlikePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                   Authentication authentication) {
        return postInteractionService.unlikePost(requireCurrentUser(authentication).userId(), postId);
    }

    @PostMapping("/{postId}/favorite")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Favorite one published post for the current authenticated user.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is unavailable
     */
    public Result<Void> favoritePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                     Authentication authentication) {
        return postInteractionService.favoritePost(requireCurrentUser(authentication).userId(), postId);
    }

    @DeleteMapping("/{postId}/favorite")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Cancel one favorite relation on the specified post.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is unavailable
     */
    public Result<Void> unfavoritePost(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                       Authentication authentication) {
        return postInteractionService.unfavoritePost(requireCurrentUser(authentication).userId(), postId);
    }

    @GetMapping("/{postId}/interaction-status")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query current user's like and favorite status for one post.
     * Params:
     * - postId: target post id
     * - authentication: spring authentication object
     * Returns:
     * - Result<InteractionStatusResponse>: interaction status payload
     * Throws:
     * - BusinessException: when current request is unauthenticated or post is unavailable
     */
    public Result<InteractionStatusResponse> getInteractionStatus(@PathVariable @Min(value = 1, message = "postId must be greater than 0") Long postId,
                                                                  Authentication authentication) {
        return postInteractionService.getInteractionStatus(requireCurrentUser(authentication).userId(), postId);
    }

    @GetMapping("/favorites/mine")
    /**
     * Author: Shuying Liang
     * Date: 2026-04-27
     * Purpose: Query all visible favorite posts of the current user.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<List<PostResponse>>: favorite post list
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<PageResponse<PostResponse>> getMyFavoritePosts(@RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                                 @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                                 Authentication authentication) {
        return postInteractionService.getMyFavoritePosts(requireCurrentUser(authentication).userId(), page, pageSize);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-27
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
}
