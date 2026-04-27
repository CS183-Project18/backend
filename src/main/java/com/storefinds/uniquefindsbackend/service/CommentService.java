package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CommentResponse;
import com.storefinds.uniquefindsbackend.dto.CreateCommentRequest;
import com.storefinds.uniquefindsbackend.dto.PageResponse;

import java.util.List;

public interface CommentService {

    Result<CommentResponse> createComment(Long userId, Long postId, CreateCommentRequest request);

    Result<PageResponse<CommentResponse>> getCommentsByPostId(Long userId, Long postId, int page, int pageSize);

    Result<Void> deleteComment(Long userId, Long postId, Long commentId);

    Result<PageResponse<CommentResponse>> getMyComments(Long userId, int page, int pageSize);
}
