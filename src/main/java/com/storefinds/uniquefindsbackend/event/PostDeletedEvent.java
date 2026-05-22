package com.storefinds.uniquefindsbackend.event;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Notify index synchronization listeners that one post was deleted from a previous moderation status.
 * Params: None
 * Returns: None
 * Throws: None
 */
public record PostDeletedEvent(Long postId, String previousStatus) {
}
