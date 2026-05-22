package com.storefinds.uniquefindsbackend.service;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-22
 * Purpose: Define the published-post AI index synchronization boundary used by startup and post lifecycle events.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface IndexSyncService {

    void scheduleRebuild(String reason);
}
