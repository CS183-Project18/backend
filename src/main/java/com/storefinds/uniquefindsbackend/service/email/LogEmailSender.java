package com.storefinds.uniquefindsbackend.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.mail.mock", havingValue = "true")
public class LogEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Log verification code instead of sending real email in mock mode.
     * Params:
     * - email: destination email
     * - code: verification code
     * - expireSeconds: expiration seconds
     * Returns: None
     * Throws: None
     */
    public void sendVerificationCode(String email, String code, long expireSeconds) {
        log.info("Mock email sender -> email={}, code={}, expiresInSeconds={}", email, code, expireSeconds);
    }
}
