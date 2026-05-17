package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define the current concrete search backend used by discovery flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface PostSearchService {

    PageResponse<Post> searchPublishedPosts(PostSearchQuery query);

    PageResponse<Post> getTrendingPosts(TrendingPostsQuery query);
}
