package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Return authenticated user identity and token data after a successful login flow.
 */
@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String token;
}
