package com.storefinds.uniquefindsbackend.common;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Define stable API error and success codes shared by controllers and exception handlers.
 * Params: None
 * Returns: None
 * Throws: None
 */
public final class ErrorCode {

    public static final String OK = "OK";
    public static final String INVALID_ARGUMENT = "INVALID_ARGUMENT";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String INVALID_STATE = "INVALID_STATE";
    public static final String BUSINESS_ERROR = "BUSINESS_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCode() {
    }
}
