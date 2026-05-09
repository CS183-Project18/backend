package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.UpdateUserProfileRequest;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Inject user profile service for current user profile endpoints.
     * Params:
     * - userProfileService: user profile business service
     * Returns: None
     * Throws: None
     */
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Query the current authenticated user's profile.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<UserProfileResponse>: current user profile
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<UserProfileResponse> getMyProfile(Authentication authentication) {
        return userProfileService.getMyProfile(requireCurrentUser(authentication).userId());
    }

    @PutMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Update editable profile fields of the current authenticated user.
     * Params:
     * - request: profile update payload
     * - authentication: spring authentication object
     * Returns:
     * - Result<UserProfileResponse>: updated user profile
     * Throws:
     * - BusinessException: when current request is unauthenticated or payload is invalid
     */
    public Result<UserProfileResponse> updateMyProfile(@RequestBody @Valid UpdateUserProfileRequest request,
                                                       Authentication authentication) {
        return userProfileService.updateMyProfile(requireCurrentUser(authentication).userId(), request);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
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
