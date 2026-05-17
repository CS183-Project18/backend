package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

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
