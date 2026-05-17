package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.InteractionStatusResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.SharePostResponse;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Define post interaction capabilities such as like, favorite, and share.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface PostInteractionService {

    Result<Void> likePost(Long userId, Long postId);

    Result<Void> unlikePost(Long userId, Long postId);

    Result<Void> favoritePost(Long userId, Long postId);

    Result<Void> unfavoritePost(Long userId, Long postId);

    Result<InteractionStatusResponse> getInteractionStatus(Long userId, Long postId);

    Result<PageResponse<PostResponse>> getMyFavoritePosts(Long userId, int page, int pageSize);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-11
     * Purpose: Build share metadata for one published post.
     * Params:
     * - postId: target post id
     * Returns:
     * - Result<SharePostResponse>: canonical share data
     * Throws: None
     */
    Result<SharePostResponse> sharePost(Long postId);
}
