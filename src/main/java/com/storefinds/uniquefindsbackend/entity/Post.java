package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Author: Shuying Liang
 * Date: 2026-05-27
 * Purpose: Represent the core post aggregate stored and searched by the backend content module.
 */
@Data
public class Post {
    private Long id;
    private Long userId;
    private Long storeId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String currency;
    private String locationText;

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    private String coverImage;
    private String status;
    private String moderationReason;
    private Long viewCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private LocalDateTime publishedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorUsername;
}
