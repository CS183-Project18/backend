package com.storefinds.uniquefindsbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Return unified JSON response for unauthorized requests.
     * Params:
     * - request: HTTP request
     * - response: HTTP response
     * - authException: spring auth exception
     * Returns: None
     * Throws:
     * - IOException: when response writing fails
     */
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\":false,\"message\":\"unauthorized\",\"data\":null}");
    }
}
