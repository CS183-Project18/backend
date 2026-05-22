package com.storefinds.uniquefindsbackend.event;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Notify index synchronization listeners that one post changed moderation status.
 * Params: None
 * Returns: None
 * Throws: None
 */
public record PostStatusChangedEvent(Long postId, String oldStatus, String newStatus) {
}
