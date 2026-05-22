package com.storefinds.uniquefindsbackend.event;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Notify index synchronization listeners that one published post changed searchable content and requires rebuild.
 * Params: None
 * Returns: None
 * Throws: None
 */
public record PostSearchContentChangedEvent(Long postId) {
}
