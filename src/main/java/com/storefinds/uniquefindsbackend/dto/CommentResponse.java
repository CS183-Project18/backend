package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Transfer comment detail data including current-user interaction state and pinned state.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class CommentResponse {
    private Long id;
    private Long postId;
    private String postTitle;
    private Long userId;
    private String username;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private String replyToUsername;
    private String content;
    private String status;
    private Long likeCount;
    private Boolean likedByCurrentUser;
    private Boolean pinned;
    private Boolean deleted;
    private Boolean ownedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
