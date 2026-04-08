package com.storefinds.uniquefindsbackend.controller.admin;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @GetMapping("/me")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-08
     * Purpose: Return current administrator identity information.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<Map<String,Object>>: admin identity payload
     * Throws:
     * - BusinessException: when request is not authenticated
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
