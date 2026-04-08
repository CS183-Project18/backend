package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CodeLoginRequest;
import com.storefinds.uniquefindsbackend.dto.LoginResponse;
import com.storefinds.uniquefindsbackend.dto.PasswordLoginRequest;
import com.storefinds.uniquefindsbackend.dto.RegisterRequest;
import com.storefinds.uniquefindsbackend.dto.SendCodeRequest;
import com.storefinds.uniquefindsbackend.dto.SendCodeResponse;
import com.storefinds.uniquefindsbackend.entity.User;
import com.storefinds.uniquefindsbackend.entity.VerificationCode;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.UserMapper;
import com.storefinds.uniquefindsbackend.mapper.VerificationCodeMapper;
import com.storefinds.uniquefindsbackend.service.AuthService;
import com.storefinds.uniquefindsbackend.service.email.EmailSender;
import com.storefinds.uniquefindsbackend.util.JwtUtil;
import com.storefinds.uniquefindsbackend.util.VerificationCodeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final long CODE_EXPIRE_SECONDS = 300L;
    private static final long SEND_COOLDOWN_SECONDS = 60L;

    private final UserMapper userMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VerificationCodeUtil verificationCodeUtil;
    private final EmailSender emailSender;
    @Value("${app.auth.debug-code-response:true}")
    private boolean debugCodeResponse;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Construct auth service with required mappers, utilities, and mail sender.
     * Params:
     * - userMapper: user data access mapper
     * - verificationCodeMapper: verification code data access mapper
     * - passwordEncoder: password hashing/verification encoder
     * - jwtUtil: JWT generator and parser utility
     * - verificationCodeUtil: verification code generator utility
     * - emailSender: email sending abstraction
     * Returns: None
     * Throws: None
     */
    public AuthServiceImpl(UserMapper userMapper,
                           VerificationCodeMapper verificationCodeMapper,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           VerificationCodeUtil verificationCodeUtil,
                           EmailSender emailSender) {
        this.userMapper = userMapper;
        this.verificationCodeMapper = verificationCodeMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.verificationCodeUtil = verificationCodeUtil;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Register a new user with username/email/password and issue JWT on success.
     * Params:
     * - request: registration request payload
     * Returns:
     * - Result<LoginResponse>: token and user profile after registration
     * Throws:
     * - BusinessException: when username/email already exists
     */
    public Result<LoginResponse> register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userMapper.selectByUsername(username) != null) {
            throw new BusinessException("username already exists");
        }
        if (userMapper.selectByEmail(email) != null) {
            throw new BusinessException("email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setEmailVerified(1);
        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        userMapper.updateLastLoginAt(user.getId());

        return Result.success("register success", buildLoginResponse(user, token));
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Authenticate user by username/email + password and issue JWT.
     * Params:
     * - request: password login payload
     * Returns:
     * - Result<LoginResponse>: token and user profile after login
     * Throws:
     * - BusinessException: when account is invalid, disabled, or password mismatch
     */
    public Result<LoginResponse> loginByPassword(PasswordLoginRequest request) {
        User user = findByAccount(request.getAccount());
        validateUserActive(user);
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("invalid account or password");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        userMapper.updateLastLoginAt(user.getId());
        return Result.success(buildLoginResponse(user, token));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Send login verification code with cooldown limit and persistence.
     * Params:
     * - request: email payload for code delivery
     * Returns:
     * - Result<SendCodeResponse>: code send status and expiry metadata
     * Throws:
     * - BusinessException: when user not found, disabled, or cooldown not reached
     */
    public Result<SendCodeResponse> sendLoginCode(SendCodeRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        validateUserActive(user);

        VerificationCode latestCode = verificationCodeMapper.selectLatestLoginCodeByEmail(email);
        if (latestCode != null && latestCode.getCreatedAt() != null) {
            LocalDateTime allowSendAt = latestCode.getCreatedAt().plusSeconds(SEND_COOLDOWN_SECONDS);
            if (allowSendAt.isAfter(LocalDateTime.now())) {
                long secondsLeft = java.time.Duration.between(LocalDateTime.now(), allowSendAt).getSeconds();
                throw new BusinessException("please wait " + Math.max(secondsLeft, 1) + " seconds before requesting another code");
            }
        }

        String code = verificationCodeUtil.generateSixDigitsCode();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUserId(user.getId());
        verificationCode.setTarget(email);
        verificationCode.setChannel("EMAIL");
        verificationCode.setPurpose("LOGIN");
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusSeconds(CODE_EXPIRE_SECONDS));
        verificationCode.setUsedAt(null);
        verificationCode.setAttemptCount(0);
        verificationCode.setMaxAttempts(5);
        verificationCode.setStatus("PENDING");
        verificationCodeMapper.insert(verificationCode);

        emailSender.sendVerificationCode(email, code, CODE_EXPIRE_SECONDS);
        String debugCode = debugCodeResponse ? code : null;
        return Result.success("verification code sent", new SendCodeResponse(email, CODE_EXPIRE_SECONDS, debugCode));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Complete login by validating pending verification code and issuing JWT.
     * Params:
     * - request: email and code payload
     * Returns:
     * - Result<LoginResponse>: token and user profile after successful verification
     * Throws:
     * - BusinessException: when code missing/expired/invalid/locked or user not found
     */
    public Result<LoginResponse> loginByCode(CodeLoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        validateUserActive(user);

        VerificationCode latestCode = verificationCodeMapper.selectLatestPendingLoginCodeByEmail(email);
        if (latestCode == null) {
            throw new BusinessException("verification code not found");
        }
        if (latestCode.getExpiresAt() != null && latestCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationCodeMapper.markAsExpired(latestCode.getId());
            throw new BusinessException("verification code expired");
        }
        if (!latestCode.getCode().equals(request.getCode().trim())) {
            verificationCodeMapper.increaseAttemptAndLockIfNeeded(latestCode.getId());
            VerificationCode refreshed = verificationCodeMapper.selectLatestPendingLoginCodeByEmail(email);
            if (refreshed == null || (refreshed.getAttemptCount() != null
                    && refreshed.getMaxAttempts() != null
                    && refreshed.getAttemptCount() >= refreshed.getMaxAttempts())) {
                throw new BusinessException("verification code is locked due to too many failed attempts");
            }
            throw new BusinessException("verification code is invalid");
        }
        verificationCodeMapper.markAsUsed(latestCode.getId());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        userMapper.updateLastLoginAt(user.getId());
        return Result.success(buildLoginResponse(user, token));
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Resolve input account string to a concrete user (username or email).
     * Params:
     * - account: raw login account
     * Returns:
     * - User: matched user entity
     * Throws:
     * - BusinessException: when account is blank or user does not exist
     */
    private User findByAccount(String account) {
        String normalized = account == null ? "" : account.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException("account is required");
        }
        User user = normalized.contains("@")
                ? userMapper.selectByEmail(normalized.toLowerCase())
                : userMapper.selectByUsername(normalized);
        if (user == null) {
            throw new BusinessException("invalid account or password");
        }
        return user;
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Ensure the user account status allows authentication.
     * Params:
     * - user: user entity to validate
     * Returns: None
     * Throws:
     * - BusinessException: when user status is not ACTIVE
     */
    private void validateUserActive(User user) {
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("user is not active");
        }
    }

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Build standardized login response object from user and token.
     * Params:
     * - user: authenticated user entity
     * - token: signed JWT token
     * Returns:
     * - LoginResponse: API response payload object
     * Throws: None
     */
    private LoginResponse buildLoginResponse(User user, String token) {
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setToken(token);
        return response;
    }
}
