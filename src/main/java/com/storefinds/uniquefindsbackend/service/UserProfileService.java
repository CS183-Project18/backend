package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.UpdateUserProfileRequest;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;

public interface UserProfileService {

    Result<UserProfileResponse> getMyProfile(Long userId);

    Result<UserProfileResponse> updateMyProfile(Long userId, UpdateUserProfileRequest request);
}
