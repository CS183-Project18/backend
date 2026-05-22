package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import org.springframework.web.multipart.MultipartFile;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Provide a stable discovery boundary that can later combine SQL search, recommendation, and AI retrieval.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface DiscoveryFacade {

    PageResponse<Post> searchPublishedPosts(PostSearchQuery query);

    PageResponse<Post> searchPublishedPostsByImage(PostSearchQuery query, MultipartFile file);

    PageResponse<Post> getTrendingPosts(TrendingPostsQuery query);
}
