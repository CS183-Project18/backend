package com.storefinds.uniquefindsbackend.security;

public record CurrentUser(Long userId, String username, String role) {
}
