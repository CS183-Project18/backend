package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Represent comment persistence data including pinned state and display helper fields.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class Comment {
    private Long id;
    private Long postId;
    private Long postOwnerId;
    private Long userId;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private String content;
    private String status;
    private Long likeCount;
    private Integer isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String postTitle;
    private String username;
    private String replyToUsername;
}
