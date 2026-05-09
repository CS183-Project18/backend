package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.UpdateUserProfileRequest;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;
import com.storefinds.uniquefindsbackend.entity.User;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.UserMapper;
import com.storefinds.uniquefindsbackend.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Inject user mapper dependency for user profile business logic.
     * Params:
     * - userMapper: user data access mapper
     * Returns: None
     * Throws: None
     */
    public UserProfileServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Query the current user's profile information.
     * Params:
     * - userId: current authenticated user id
     * Returns:
     * - Result<UserProfileResponse>: current user profile
     * Throws:
     * - BusinessException: when user does not exist
     */
    public Result<UserProfileResponse> getMyProfile(Long userId) {
        return Result.success(toUserProfileResponse(requireUser(userId)));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Update editable profile fields of the current user.
     * Params:
     * - userId: current authenticated user id
     * - request: profile update payload
     * Returns:
     * - Result<UserProfileResponse>: updated user profile
     * Throws:
     * - BusinessException: when user does not exist
     */
    public Result<UserProfileResponse> updateMyProfile(Long userId, UpdateUserProfileRequest request) {
        User user = requireUser(userId);
        user.setNickname(normalizeOptionalText(request.getNickname()));
        user.setAvatarUrl(normalizeOptionalText(request.getAvatarUrl()));
        user.setBio(normalizeOptionalText(request.getBio()));
        userMapper.updateProfile(user);
        return Result.success("profile updated", toUserProfileResponse(requireUser(userId)));
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Ensure the target user exists.
     * Params:
     * - userId: target user id
     * Returns:
     * - User: matched user entity
     * Throws:
     * - BusinessException: when user does not exist
     */
    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        return user;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Trim one optional text field and convert blank content to null.
     * Params:
     * - value: raw text value
     * Returns:
     * - String: normalized text value or null
     * Throws: None
     */
    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Convert user entity to user profile response object.
     * Params:
     * - user: source user entity
     * Returns:
     * - UserProfileResponse: response payload object
     * Throws: None
     */
    private UserProfileResponse toUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        return response;
    }
}
