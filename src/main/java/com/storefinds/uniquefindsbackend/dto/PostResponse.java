package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {
    private Long id;
    private Long userId;
    private String authorUsername;
    private Long storeId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String currency;
    private String locationText;
    private String status;
    private String moderationReason;
    private Long viewCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private Boolean likedByCurrentUser;
    private Boolean favoritedByCurrentUser;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PostImageResponse> images;
}
