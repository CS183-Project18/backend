package com.storefinds.uniquefindsbackend.service;

import com.storefinds.uniquefindsbackend.entity.Notification;
import org.springframework.stereotype.Component;

@Component
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-14
 * Purpose: Build normalized notification entities from structured event inputs.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class NotificationEventFactory {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Build one notification entity with default unread state.
     * Params:
     * - recipientUserId: notification receiver id
     * - actorUserId: notification actor id
     * - eventType: stable event type value
     * - targetType: stable target type value
     * - targetId: target entity id
     * - postId: related post id
     * - metadata: optional structured metadata JSON
     * Returns:
     * - Notification: normalized notification entity
     * Throws: None
     */
    public Notification build(Long recipientUserId,
                              Long actorUserId,
                              String eventType,
                              String targetType,
                              Long targetId,
                              Long postId,
                              String metadata) {
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setActorUserId(actorUserId);
        notification.setEventType(eventType);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setPostId(postId);
        notification.setMetadata(metadata);
        notification.setIsRead(0);
        return notification;
    }
}
