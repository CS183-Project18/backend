package com.storefinds.uniquefindsbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendCodeRequest {
    @NotBlank(message = "email is required")
    @Email(message = "email is invalid")
    private String email;
}
