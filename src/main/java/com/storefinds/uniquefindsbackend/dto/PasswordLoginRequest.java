package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordLoginRequest {
    @NotBlank(message = "account is required")
    private String account;

    @NotBlank(message = "password is required")
    private String password;
}
