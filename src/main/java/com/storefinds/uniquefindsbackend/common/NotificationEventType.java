package com.storefinds.uniquefindsbackend.common;

import java.util.Set;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-10
 * Purpose: Define stable notification event type constants shared across interaction and moderation flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class NotificationEventType {

    public static final String POST_LIKED = "POST_LIKED";
    public static final String POST_FAVORITED = "POST_FAVORITED";
    public static final String COMMENT_REPLIED = "COMMENT_REPLIED";
    public static final String COMMENT_LIKED = "COMMENT_LIKED";
    public static final String COMMENT_PINNED = "COMMENT_PINNED";
    public static final String POST_MODERATED = "POST_MODERATED";
    public static final String COMMENT_MODERATED = "COMMENT_MODERATED";

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            POST_LIKED,
            POST_FAVORITED,
            COMMENT_REPLIED,
            COMMENT_LIKED,
            COMMENT_PINNED,
            POST_MODERATED,
            COMMENT_MODERATED
    );

    private NotificationEventType() {
    }

    public static boolean isSupported(String eventType) {
        return eventType != null && SUPPORTED_TYPES.contains(eventType);
    }
}
