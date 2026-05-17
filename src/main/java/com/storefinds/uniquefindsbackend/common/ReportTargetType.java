package com.storefinds.uniquefindsbackend.common;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define stable report target types shared by user reporting and admin moderation flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class ReportTargetType {

    public static final String POST = "POST";
    public static final String COMMENT = "COMMENT";
    public static final String USER = "USER";

    private ReportTargetType() {
    }
}
