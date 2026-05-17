package com.storefinds.uniquefindsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-11
 * Purpose: Transfer canonical share metadata for one post.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class SharePostResponse {
    private Long postId;
    private String shareUrl;
}
