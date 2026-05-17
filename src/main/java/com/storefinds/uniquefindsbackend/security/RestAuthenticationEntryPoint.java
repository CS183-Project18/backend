package com.storefinds.uniquefindsbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

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
        log.warn("unauthorized request: method={}, uri={}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"unauthorized\",\"data\":null}");
    }
}
