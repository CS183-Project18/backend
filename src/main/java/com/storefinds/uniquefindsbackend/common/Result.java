package com.storefinds.uniquefindsbackend.common;

public record Result<T>(boolean success, String code, String message, T data) {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Build successful result with default message.
     * Params:
     * - data: response payload data
     * Returns:
     * - Result<T>: standardized success response
     * Throws: None
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, ErrorCode.OK, "ok", data);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Build successful result with custom message.
     * Params:
     * - message: success message
     * - data: response payload data
     * Returns:
     * - Result<T>: standardized success response
     * Throws: None
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, ErrorCode.OK, message, data);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Build failed result with message only.
     * Params:
     * - message: error description
     * Returns:
     * - Result<T>: standardized error response
     * Throws: None
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(false, ErrorCode.BUSINESS_ERROR, message, null);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Build failed result with stable error code and readable message.
     * Params:
     * - code: stable error code
     * - message: error description
     * Returns:
     * - Result<T>: standardized error response
     * Throws: None
     */
    public static <T> Result<T> error(String code, String message) {
        return new Result<>(false, code, message, null);
    }
}
