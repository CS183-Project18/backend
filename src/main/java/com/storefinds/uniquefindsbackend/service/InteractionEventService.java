package com.storefinds.uniquefindsbackend.service;

import java.util.Map;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-15
 * Purpose: Define the lightweight interaction event recording capability shared by social and governance services.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface InteractionEventService {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-15
     * Purpose: Persist one normalized interaction event with optional structured metadata.
     * Params:
     * - eventType: stable event type
     * - userId: actor user id
     * - postId: related post id
     * - commentId: related comment id
     * - targetType: target type value
     * - targetId: target entity id
     * - metadata: optional structured metadata
     * Returns: None
     * Throws: None
     */
    void record(String eventType,
                Long userId,
                Long postId,
                Long commentId,
                String targetType,
                Long targetId,
                Map<String, Object> metadata);
}
