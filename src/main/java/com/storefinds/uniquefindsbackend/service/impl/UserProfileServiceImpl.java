package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.UpdateUserProfileRequest;
import com.storefinds.uniquefindsbackend.dto.UserProfileResponse;
import com.storefinds.uniquefindsbackend.entity.User;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.PostFavoriteMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.mapper.UserMapper;
import com.storefinds.uniquefindsbackend.service.PostService;
import com.storefinds.uniquefindsbackend.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final CommentMapper commentMapper;
    private final PostService postService;

    public UserProfileServiceImpl(UserMapper userMapper,
                                  PostMapper postMapper,
                                  PostFavoriteMapper postFavoriteMapper,
                                  CommentMapper commentMapper,
                                  PostService postService) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.commentMapper = commentMapper;
        this.postService = postService;
    }

    @Override
    public Result<UserProfileResponse> getMyProfile(Long userId) {
        return Result.success(toUserProfileResponse(requireUser(userId)));
    }

    @Override
    public Result<UserProfileResponse> getPublicProfile(String username) {
        User user = userMapper.selectPublicByUsername(normalizeRequiredText(username, "username is required"));
        if (user == null) {
            throw new BusinessException("user not found");
        }
        return Result.success(toUserProfileResponse(user));
    }

    @Override
    public Result<PageResponse<PostResponse>> getPublicPosts(String username, int page, int pageSize) {
        User user = userMapper.selectPublicByUsername(normalizeRequiredText(username, "username is required"));
        if (user == null) {
            throw new BusinessException("user not found");
        }
        PageResponse<PostResponse> response = new PageResponse<>();
        response.setTotal(postMapper.countPublishedByUserId(user.getId()));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems(postService.buildPostResponsesForUser(
                null,
                postMapper.selectPublishedByUserIdPage(user.getId(), toOffset(page, pageSize), pageSize)
        ));
        return Result.success(response);
    }

    @Override
    @Transactional
    public Result<UserProfileResponse> updateMyProfile(Long userId, UpdateUserProfileRequest request) {
        User user = requireUser(userId);
        user.setNickname(normalizeOptionalText(request.getNickname()));
        user.setAvatarUrl(normalizeOptionalText(request.getAvatarUrl()));
        user.setBio(normalizeOptionalText(request.getBio()));
        userMapper.updateProfile(user);
        return Result.success("profile updated", toUserProfileResponse(requireUser(userId)));
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        return user;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BusinessException(errorMessage);
        }
        return normalized;
    }

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setBio(user.getBio());
        response.setRole(user.getRole());
        response.setPostCount(postMapper.countByUserId(user.getId()));
        response.setPublishedPostCount(postMapper.countPublishedByUserId(user.getId()));
        response.setCommentCount(commentMapper.countByUserId(user.getId()));
        response.setFavoriteCount(postFavoriteMapper.countFavoritePostsByUserId(user.getId()));
        return response;
    }
}
