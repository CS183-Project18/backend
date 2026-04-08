package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.CodeLoginRequest;
import com.storefinds.uniquefindsbackend.dto.LoginResponse;
import com.storefinds.uniquefindsbackend.dto.PasswordLoginRequest;
import com.storefinds.uniquefindsbackend.dto.RegisterRequest;
import com.storefinds.uniquefindsbackend.dto.SendCodeRequest;
import com.storefinds.uniquefindsbackend.dto.SendCodeResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.service.AuthService;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    private final AuthService authService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Inject authentication service for user auth endpoints.
     * Params:
     * - authService: authentication business service
     * Returns: None
     * Throws: None
     */
    public UserAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Register a normal user account.
     * Params:
     * - request: register payload
     * Returns:
     * - Result<LoginResponse>: created user and JWT token
     * Throws:
     * - BusinessException: when username/email already exists
     */
    public Result<LoginResponse> register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login/password")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Login by password using username or email.
     * Params:
     * - request: password login payload
     * Returns:
     * - Result<LoginResponse>: authenticated user and JWT token
     * Throws:
     * - BusinessException: when credentials are invalid
     */
    public Result<LoginResponse> loginByPassword(@RequestBody @Valid PasswordLoginRequest request) {
        return authService.loginByPassword(request);
    }

    @PostMapping("/code/send")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Send email verification code for code-based login.
     * Params:
     * - request: email payload
     * Returns:
     * - Result<SendCodeResponse>: send result and expiration info
     * Throws:
     * - BusinessException: when cooldown/validation checks fail
     */
    public Result<SendCodeResponse> sendCode(@RequestBody @Valid SendCodeRequest request) {
        return authService.sendLoginCode(request);
    }

    @PostMapping("/login/code")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Login by email verification code.
     * Params:
     * - request: email + code payload
     * Returns:
     * - Result<LoginResponse>: authenticated user and JWT token
     * Throws:
     * - BusinessException: when code is invalid, expired, or locked
     */
    public Result<LoginResponse> loginByCode(@RequestBody @Valid CodeLoginRequest request) {
        return authService.loginByCode(request);
    }

    @GetMapping("/me")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Return current authenticated user info from security context.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<Map<String,Object>>: userId/username/role info
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return Result.success(Map.of(
                "userId", currentUser.userId(),
                "username", currentUser.username(),
                "role", currentUser.role()
        ));
    }
}
