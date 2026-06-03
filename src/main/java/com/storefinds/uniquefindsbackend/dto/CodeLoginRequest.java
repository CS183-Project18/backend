package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Capture the email-and-code payload used by the code-based login module.
 */
@Data
public class CodeLoginRequest {
    @NotBlank(message = "email is required")
    @Email(message = "email is invalid")
    private String email;

    @NotBlank(message = "code is required")
    private String code;
}
