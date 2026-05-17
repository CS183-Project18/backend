package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateUserProfileRequest;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Define current-user and public-profile capabilities for user profile APIs.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface UserProfileService {

    Result<UserProfileResponse> getMyProfile(Long userId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Query public profile detail by username.
     * Params:
     * - username: target username
     * Returns:
     * - Result<UserProfileResponse>: public profile detail
     * Throws: None
     */
    Result<UserProfileResponse> getPublicProfile(String username);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Query one page of published posts for a public user profile.
     * Params:
     * - username: target username
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - Result<PageResponse<PostResponse>>: public post page
     * Throws: None
     */
    Result<PageResponse<PostResponse>> getPublicPosts(String username, int page, int pageSize);

    Result<UserProfileResponse> updateMyProfile(Long userId, UpdateUserProfileRequest request);
}
