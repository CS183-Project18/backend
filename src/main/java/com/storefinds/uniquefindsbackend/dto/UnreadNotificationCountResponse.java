package com.storefinds.uniquefindsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Transfer unread notification counter data for the current user.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class UnreadNotificationCountResponse {
    private long unreadCount;
}
