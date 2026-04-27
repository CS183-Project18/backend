package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
    private Boolean deleted;
    private Boolean ownedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
