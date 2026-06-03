package com.storefinds.uniquefindsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Return verification-code send status and expiry details for the auth module.
 */
@Data
@AllArgsConstructor
public class SendCodeResponse {
    private String email;
    private long expiresInSeconds;
    private String debugCode;
}
