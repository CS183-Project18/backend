package com.storefinds.uniquefindsbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Represent the persisted user account used by authentication and profile flows.
 */
@Data
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String role;
    private String status;
    private Integer emailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
