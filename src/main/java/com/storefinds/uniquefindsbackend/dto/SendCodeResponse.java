package com.storefinds.uniquefindsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendCodeResponse {
    private String email;
    private long expiresInSeconds;
    private String debugCode;
}
