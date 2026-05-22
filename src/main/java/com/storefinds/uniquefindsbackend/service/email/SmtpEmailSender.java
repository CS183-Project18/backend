package com.storefinds.uniquefindsbackend.service.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@ConditionalOnProperty(name = "app.mail.mock", havingValue = "false", matchIfMissing = true)
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Construct SMTP sender with spring mail sender bean.
     * Params:
     * - javaMailSender: spring mail sender
     * Returns: None
     * Throws: None
     */
    public SmtpEmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Send login verification code email through SMTP provider.
     * Params:
     * - email: destination email
     * - code: verification code
     * - expireSeconds: code expiration seconds
     * Returns: None
     * Throws:
     * - RuntimeException: when email sending fails
     */
    public void sendVerificationCode(String email, String code, long expireSeconds) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("Unique Finds <" + sendMailer + ">");
            helper.setTo(email);
            helper.setSubject("Unique Finds Verification Code");
            helper.setText("Your verification code is: " + code
                    + ". It will expire in " + (expireSeconds / 60)
                    + " minutes. Please do not share this code with anyone.");
            helper.setSentDate(new Date());
            javaMailSender.send(mimeMessage);
        } catch (Exception ex) {
            throw new RuntimeException("failed to send verification email", ex);
        }
    }
}
