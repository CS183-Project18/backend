package com.storefinds.uniquefindsbackend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Return unified JSON response for authenticated requests lacking permission.
     * Params:
     * - request: HTTP request
     * - response: HTTP response
     * - accessDeniedException: spring access denied exception
     * Returns: None
     * Throws:
     * - IOException: when response writing fails
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\":false,\"code\":\"FORBIDDEN\",\"message\":\"forbidden\",\"data\":null}");
    }
}
