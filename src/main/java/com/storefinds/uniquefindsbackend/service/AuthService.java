package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CodeLoginRequest;
import com.storefinds.uniquefindsbackend.dto.LoginResponse;
import com.storefinds.uniquefindsbackend.dto.PasswordLoginRequest;
import com.storefinds.uniquefindsbackend.dto.RegisterRequest;
import com.storefinds.uniquefindsbackend.dto.SendCodeRequest;
import com.storefinds.uniquefindsbackend.dto.SendCodeResponse;

public interface AuthService {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Register user account and return login token.
     * Params:
     * - request: registration payload
     * Returns:
     * - Result<LoginResponse>: registration result with token
     * Throws:
     * - BusinessException: when validation/business rules fail
     */
    Result<LoginResponse> register(RegisterRequest request);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Login by password.
     * Params:
     * - request: account/password payload
     * Returns:
     * - Result<LoginResponse>: login result with token
     * Throws:
     * - BusinessException: when account validation fails
     */
    Result<LoginResponse> loginByPassword(PasswordLoginRequest request);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Send login verification code.
     * Params:
     * - request: email payload
     * Returns:
     * - Result<SendCodeResponse>: code send result
     * Throws:
     * - BusinessException: when cooldown/validation fails
     */
    Result<SendCodeResponse> sendLoginCode(SendCodeRequest request);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Login by verification code.
     * Params:
     * - request: email/code payload
     * Returns:
     * - Result<LoginResponse>: login result with token
     * Throws:
     * - BusinessException: when code is invalid/expired/locked
     */
    Result<LoginResponse> loginByCode(CodeLoginRequest request);
}
