package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreatePostRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.UpdatePostRequest;
import com.storefinds.uniquefindsbackend.entity.Post;

import java.util.List;

public interface PostService {

    Result<PostResponse> createPost(Long userId, CreatePostRequest request);

    Result<PostResponse> getPostById(Long userId, String userRole, Long postId);

    Result<PageResponse<PostResponse>> getPublishedPosts(Long userId, int page, int pageSize);

    Result<PageResponse<PostResponse>> getMyPosts(Long userId, int page, int pageSize);

    Result<PageResponse<PostResponse>> searchPublishedPosts(Long userId,
                                                            String keyword,
                                                            Long categoryId,
                                                            String sort,
                                                            int page,
                                                            int pageSize);

    Result<PageResponse<PostResponse>> getTrendingPosts(Long userId,
                                                        String window,
                                                        int page,
                                                        int pageSize);

    Result<PostResponse> updatePost(Long userId, Long postId, UpdatePostRequest request);

    Result<Void> deletePost(Long userId, Long postId);

    List<PostResponse> buildPostResponsesForUser(Long userId, List<Post> posts);

    void likePost(Long userId, Long postId);

    void unlikePost(Long userId, Long postId);

    List<Long> getLikedPostIds(Long userId, List<Long> postIds);

    boolean isLiked(Long userId, Long postId);
}
