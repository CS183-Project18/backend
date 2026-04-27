package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.InteractionStatusResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;

import java.util.List;

public interface PostInteractionService {

    Result<Void> likePost(Long userId, Long postId);

    Result<Void> unlikePost(Long userId, Long postId);

    Result<Void> favoritePost(Long userId, Long postId);

    Result<Void> unfavoritePost(Long userId, Long postId);

    Result<InteractionStatusResponse> getInteractionStatus(Long userId, Long postId);

    Result<List<PostResponse>> getMyFavoritePosts(Long userId);
}
