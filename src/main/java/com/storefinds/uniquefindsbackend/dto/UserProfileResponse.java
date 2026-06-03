package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

/**
 * Author: Enqi Guo
 * Date: 2026-05-27
 * Purpose: Return public or private profile data together with user activity counters.
 */
@Data
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String role;
    private Long postCount;
    private Long publishedPostCount;
    private Long commentCount;
    private Long favoriteCount;
}
