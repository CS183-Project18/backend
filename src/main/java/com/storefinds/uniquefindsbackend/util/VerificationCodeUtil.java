package com.storefinds.uniquefindsbackend.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationCodeUtil {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Generate a random six-digit numeric verification code.
     * Params: None
     * Returns:
     * - String: six-digit verification code
     * Throws: None
     */
    public String generateSixDigitsCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
