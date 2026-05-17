package com.storefinds.uniquefindsbackend.common;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define stable report workflow status values for moderation and reporting flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class ReportStatus {

    public static final String PENDING = "PENDING";
    public static final String PROCESSING = "PROCESSING";
    public static final String RESOLVED = "RESOLVED";
    public static final String REJECTED = "REJECTED";

    private ReportStatus() {
    }
}
