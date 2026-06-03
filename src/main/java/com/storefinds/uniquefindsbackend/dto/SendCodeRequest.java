package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Capture the email target used when requesting a login verification code.
 */
@Data
public class SendCodeRequest {
    @NotBlank(message = "email is required")
    @Email(message = "email is invalid")
    private String email;
}
