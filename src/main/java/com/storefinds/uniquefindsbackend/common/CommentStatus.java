package com.storefinds.uniquefindsbackend.common;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define stable comment status values shared by service-layer moderation and display logic.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class CommentStatus {

    public static final String VISIBLE = "VISIBLE";
    public static final String HIDDEN = "HIDDEN";
    public static final String DELETED = "DELETED";

    private CommentStatus() {
    }
}
