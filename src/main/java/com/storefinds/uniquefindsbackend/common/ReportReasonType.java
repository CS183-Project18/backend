package com.storefinds.uniquefindsbackend.common;

import java.util.Set;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define supported report reason values in one shared location for report validation logic.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class ReportReasonType {

    public static final String SPAM = "SPAM";
    public static final String ILLEGAL = "ILLEGAL";
    public static final String ABUSE = "ABUSE";
    public static final String PORN = "PORN";
    public static final String MISLEADING = "MISLEADING";
    public static final String OTHER = "OTHER";

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            SPAM,
            ILLEGAL,
            ABUSE,
            PORN,
            MISLEADING,
            OTHER
    );

    private ReportReasonType() {
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Check whether one report reason type is supported.
     * Params:
     * - reasonType: candidate report reason type
     * Returns:
     * - boolean: true when supported
     * Throws: None
     */
    public static boolean isSupported(String reasonType) {
        return reasonType != null && SUPPORTED_TYPES.contains(reasonType);
    }
}
