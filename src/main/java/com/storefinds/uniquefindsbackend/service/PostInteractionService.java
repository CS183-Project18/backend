package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.InteractionStatusResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;

public interface PostInteractionService {

    Result<Void> likePost(Long userId, Long postId);

    Result<Void> unlikePost(Long userId, Long postId);

    Result<Void> favoritePost(Long userId, Long postId);

    Result<Void> unfavoritePost(Long userId, Long postId);

    Result<InteractionStatusResponse> getInteractionStatus(Long userId, Long postId);

    Result<PageResponse<PostResponse>> getMyFavoritePosts(Long userId, int page, int pageSize);
}
