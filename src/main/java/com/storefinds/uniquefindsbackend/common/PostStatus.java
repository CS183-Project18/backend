package com.storefinds.uniquefindsbackend.common;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define stable post status values shared by service-layer visibility and moderation logic.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class PostStatus {

    public static final String PENDING_REVIEW = "PENDING_REVIEW";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String HIDDEN = "HIDDEN";
    public static final String REJECTED = "REJECTED";
    public static final String DELETED = "DELETED";

    private PostStatus() {
    }
}
