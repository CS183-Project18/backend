package com.storefinds.uniquefindsbackend.common;

import java.util.Set;

/**
 * Author: Enqi Guo
 * Date: 2026-05-16
 * Purpose: Define stable moderation action values shared by governance workflows and audit responses.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class ModerationActionType {

    public static final String APPROVE = "APPROVE";
    public static final String REJECT = "REJECT";
    public static final String HIDE = "HIDE";
    public static final String UNHIDE = "UNHIDE";
    public static final String DELETE = "DELETE";
    public static final String TARGET_MODERATED = "TARGET_MODERATED";

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            APPROVE,
            REJECT,
            HIDE,
            UNHIDE,
            DELETE,
            TARGET_MODERATED
    );

    private ModerationActionType() {
    }

    /**
     * Author: Enqi Guo
     * Date: 2026-05-16
     * Purpose: Check whether one moderation action value is supported.
     * Params:
     * - action: candidate moderation action value
     * Returns:
     * - boolean: true when supported
     * Throws: None
     */
    public static boolean isSupported(String action) {
        return action != null && SUPPORTED_TYPES.contains(action);
    }
}
