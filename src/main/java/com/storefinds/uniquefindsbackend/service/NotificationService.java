package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.NotificationResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.UnreadNotificationCountResponse;

/**
 * Author: Kaijie Zhu
 * Date: 2026-05-12
 * Purpose: Define notification query, read-state update, and notification creation capabilities.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface NotificationService {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Query one page of notifications owned by a user.
     * Params:
     * - userId: recipient user id
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - Result<PageResponse<NotificationResponse>>: notification page
     * Throws: None
     */
    Result<PageResponse<NotificationResponse>> getMyNotifications(Long userId, String eventType, int page, int pageSize);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Query unread notification count owned by a user.
     * Params:
     * - userId: recipient user id
     * Returns:
     * - Result<UnreadNotificationCountResponse>: unread counter payload
     * Throws: None
     */
    Result<UnreadNotificationCountResponse> getUnreadCount(Long userId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Mark one notification as read for its owner.
     * Params:
     * - userId: recipient user id
     * - notificationId: target notification id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    Result<Void> markAsRead(Long userId, Long notificationId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Mark all notifications as read for one user.
     * Params:
     * - userId: recipient user id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    Result<Void> markAllAsRead(Long userId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Persist one structured notification event when the actor and recipient are different users.
     * Params:
     * - recipientUserId: notification receiver id
     * - actorUserId: notification actor id
     * - eventType: stable event type value
     * - targetType: stable target type value
     * - targetId: target entity id
     * - postId: related post id for routing
     * Returns: None
     * Throws: None
     */
    void createNotification(Long recipientUserId,
                            Long actorUserId,
                            String eventType,
                            String targetType,
                            Long targetId,
                            Long postId);
}
