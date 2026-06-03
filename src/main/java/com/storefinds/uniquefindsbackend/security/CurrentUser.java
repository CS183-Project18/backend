package com.storefinds.uniquefindsbackend.security;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-27
 * Purpose: Carry the authenticated user identity resolved for controller and service entry points.
 */
public record CurrentUser(Long userId, String username, String role) {
}
