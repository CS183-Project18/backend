package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Represent one verification-code record used by the email-based login workflow.
 */
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
