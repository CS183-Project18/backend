package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;
import com.storefinds.uniquefindsbackend.service.UserProfileService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/users/{username}")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-11
 * Purpose: Expose public profile and public post list endpoints for a target username.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class PublicUserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Inject user profile service for public user profile endpoints.
     * Params:
     * - userProfileService: user profile business service
     * Returns: None
     * Throws: None
     */
    public PublicUserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Query public profile fields for one username.
     * Params:
     * - username: target username
     * Returns:
     * - Result<UserProfileResponse>: public profile detail
     * Throws: None
     */
    public Result<UserProfileResponse> getPublicProfile(@PathVariable String username) {
        return userProfileService.getPublicProfile(username);
    }

    @GetMapping("/posts")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Query one page of public posts created by the target username.
     * Params:
     * - username: target username
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - Result<PageResponse<PostResponse>>: public post page
     * Throws: None
     */
    public Result<PageResponse<PostResponse>> getPublicPosts(@PathVariable String username,
                                                             @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                             @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize) {
        return userProfileService.getPublicPosts(username, page, pageSize);
    }
}
