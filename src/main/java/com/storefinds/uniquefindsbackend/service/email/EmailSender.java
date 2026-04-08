package com.storefinds.uniquefindsbackend.service.email;

public interface EmailSender {
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Send verification code through configured email channel.
     * Params:
     * - email: destination email
     * - code: generated verification code
     * - expireSeconds: code expiration in seconds
     * Returns: None
     * Throws:
     * - RuntimeException: when concrete sender fails
     */
    void sendVerificationCode(String email, String code, long expireSeconds);
}
