package com.storefinds.uniquefindsbackend.config;

import com.storefinds.uniquefindsbackend.security.JwtAuthenticationFilter;
import com.storefinds.uniquefindsbackend.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Inject security filter and unauthorized response entry point.
     * Params:
     * - jwtAuthenticationFilter: JWT authentication filter
     * - restAuthenticationEntryPoint: custom unauthorized entry point
     * Returns: None
     * Throws: None
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Configure Spring Security authorization rules and JWT filter chain.
     * Params:
     * - http: HttpSecurity builder
     * Returns:
     * - SecurityFilterChain: built filter chain bean
     * Throws:
     * - Exception: when security chain building fails
     */
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login/password",
                                "/api/auth/login/code",
                                "/api/auth/code/send"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/published").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-07
     * Purpose: Provide password encoder bean for hashing and verification.
     * Params: None
     * Returns:
     * - PasswordEncoder: BCrypt password encoder
     * Throws: None
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
