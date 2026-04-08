package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationCode {
    private Long id;
    private Long userId;
    private String target;
    private String channel;
    private String purpose;
    private String code;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private Integer attemptCount;
    private Integer maxAttempts;
    private String status;
    private LocalDateTime createdAt;
}
