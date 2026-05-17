package com.storefinds.uniquefindsbackend.common;

import java.util.Set;

/**
 * Author: Enqi Guo
 * Date: 2026-05-15
 * Purpose: Define stable interaction event type constants used for lightweight analytics event persistence.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class InteractionEventType {

    public static final String POST_CREATE = "POST_CREATE";
    public static final String POST_VIEW = "POST_VIEW";
    public static final String POST_LIKE = "POST_LIKE";
    public static final String POST_UNLIKE = "POST_UNLIKE";
    public static final String POST_FAVORITE = "POST_FAVORITE";
    public static final String POST_UNFAVORITE = "POST_UNFAVORITE";
    public static final String COMMENT_CREATE = "COMMENT_CREATE";
    public static final String COMMENT_DELETE = "COMMENT_DELETE";
    public static final String SEARCH_REQUEST = "SEARCH_REQUEST";
    public static final String REPORT_SUBMIT = "REPORT_SUBMIT";
    public static final String REPORT_CLOSE = "REPORT_CLOSE";
    public static final String SHARE_LINK_CREATE = "SHARE_LINK_CREATE";

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            POST_CREATE,
            POST_VIEW,
            POST_LIKE,
            POST_UNLIKE,
            POST_FAVORITE,
            POST_UNFAVORITE,
            COMMENT_CREATE,
            COMMENT_DELETE,
            SEARCH_REQUEST,
            REPORT_SUBMIT,
            REPORT_CLOSE,
            SHARE_LINK_CREATE
    );

    private InteractionEventType() {
    }

    /**
     * Author: Enqi Guo
     * Date: 2026-05-15
     * Purpose: Check whether one interaction event type is supported by the backend event ledger.
     * Params:
     * - eventType: candidate event type
     * Returns:
     * - boolean: true when supported
     * Throws: None
     */
    public static boolean isSupported(String eventType) {
        return eventType != null && SUPPORTED_TYPES.contains(eventType);
    }
}
