package com.storefinds.uniquefindsbackend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String token;
}
