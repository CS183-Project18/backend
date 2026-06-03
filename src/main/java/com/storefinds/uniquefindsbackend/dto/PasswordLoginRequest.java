package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Capture the account-and-password payload used by the password login module.
 */
@Data
public class PasswordLoginRequest {
    @NotBlank(message = "account is required")
    private String account;

    @NotBlank(message = "password is required")
    private String password;
}
