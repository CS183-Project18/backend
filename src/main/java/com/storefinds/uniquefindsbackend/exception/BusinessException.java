package com.storefinds.uniquefindsbackend.exception;

import com.storefinds.uniquefindsbackend.common.ErrorCode;

public class BusinessException extends RuntimeException {

    private final String code;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Create business exception with readable message.
     * Params:
     * - message: business error message
     * Returns: None
     * Throws: None
     */
    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_ERROR, message);
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Create business exception with a stable error code and readable message.
     * Params:
     * - code: stable error code
     * - message: business error message
     * Returns: None
     * Throws: None
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
