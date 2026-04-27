package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CreatePostRequest;
import com.storefinds.uniquefindsbackend.dto.PostResponse;
import com.storefinds.uniquefindsbackend.dto.UpdatePostRequest;
import com.storefinds.uniquefindsbackend.entity.Post;

import java.util.List;

public interface PostService {

    Result<PostResponse> createPost(Long userId, CreatePostRequest request);

    Result<PostResponse> getPostById(Long userId, Long postId);

    Result<List<PostResponse>> getPublishedPosts(Long userId);

    Result<List<PostResponse>> getMyPosts(Long userId);

    Result<PostResponse> updatePost(Long userId, Long postId, UpdatePostRequest request);

    Result<Void> deletePost(Long userId, Long postId);

    List<PostResponse> buildPostResponsesForUser(Long userId, List<Post> posts);
}
