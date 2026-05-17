package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.PostSearchQuery;
import com.storefinds.uniquefindsbackend.dto.TrendingPostsQuery;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.service.DiscoveryFacade;
import com.storefinds.uniquefindsbackend.service.PostSearchService;
import org.springframework.stereotype.Service;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Provide the current discovery orchestration boundary for SQL search and future AI retrieval extensions.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class DefaultDiscoveryFacade implements DiscoveryFacade {

    private final PostSearchService postSearchService;

    public DefaultDiscoveryFacade(PostSearchService postSearchService) {
        this.postSearchService = postSearchService;
    }

    @Override
    public PageResponse<Post> searchPublishedPosts(PostSearchQuery query) {
        return postSearchService.searchPublishedPosts(query);
    }

    @Override
    public PageResponse<Post> getTrendingPosts(TrendingPostsQuery query) {
        return postSearchService.getTrendingPosts(query);
    }
}
