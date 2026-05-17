package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.common.ErrorCode;
import com.storefinds.uniquefindsbackend.common.NotificationEventType;
import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.NotificationResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.UnreadNotificationCountResponse;
import com.storefinds.uniquefindsbackend.entity.Notification;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.NotificationMapper;
import com.storefinds.uniquefindsbackend.service.NotificationEventFactory;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-13
 * Purpose: Implement notification pagination, read-state updates, and structured notification creation.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationEventFactory notificationEventFactory;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Inject notification mapper for notification persistence operations.
     * Params:
     * - notificationMapper: notification data access mapper
     * Returns: None
     * Throws: None
     */
    public NotificationServiceImpl(NotificationMapper notificationMapper,
                                   NotificationEventFactory notificationEventFactory) {
        this.notificationMapper = notificationMapper;
        this.notificationEventFactory = notificationEventFactory;
    }

    @Override
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Query one page of notifications owned by a user.
     * Params:
     * - userId: recipient user id
     * - page: requested page number
     * - pageSize: requested page size
     * Returns:
     * - Result<PageResponse<NotificationResponse>>: notification page
     * Throws: None
     */
    public Result<PageResponse<NotificationResponse>> getMyNotifications(Long userId, String eventType, int page, int pageSize) {
        String normalizedEventType = normalizeEventType(eventType);
        PageResponse<NotificationResponse> response = new PageResponse<>();
        response.setTotal(normalizedEventType == null
                ? notificationMapper.countByRecipientUserId(userId)
                : notificationMapper.countByRecipientUserIdAndEventType(userId, normalizedEventType));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setItems((normalizedEventType == null
                ? notificationMapper.selectByRecipientUserId(userId, toOffset(page, pageSize), pageSize)
                : notificationMapper.selectByRecipientUserIdAndEventType(userId, normalizedEventType, toOffset(page, pageSize), pageSize))
                .stream()
                .map(this::toNotificationResponse)
                .toList());
        return Result.success(response);
    }

    @Override
    public Result<UnreadNotificationCountResponse> getUnreadCount(Long userId) {
        return Result.success(new UnreadNotificationCountResponse(
                notificationMapper.countUnreadByRecipientUserId(userId)
        ));
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Mark one notification as read after verifying ownership.
     * Params:
     * - userId: recipient user id
     * - notificationId: target notification id
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when the notification does not belong to the current user
     */
    public Result<Void> markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !userId.equals(notification.getRecipientUserId())) {
            throw new BusinessException("notification not found");
        }
        notificationMapper.markAsRead(notificationId, userId);
        return Result.success("notification marked as read", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Mark all notifications owned by one user as read.
     * Params:
     * - userId: recipient user id
     * Returns:
     * - Result<Void>: operation result
     * Throws: None
     */
    public Result<Void> markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
        return Result.success("all notifications marked as read", null);
    }

    @Override
    @Transactional
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-14
     * Purpose: Persist one notification record while skipping self-triggered events.
     * Params:
     * - recipientUserId: notification receiver id
     * - actorUserId: notification actor id
     * - eventType: stable event type value
     * - targetType: stable target type value
     * - targetId: target entity id
     * - postId: related post id
     * Returns: None
     * Throws: None
     */
    public void createNotification(Long recipientUserId,
                                   Long actorUserId,
                                   String eventType,
                                   String targetType,
                                   Long targetId,
                                   Long postId) {
        if (recipientUserId == null || recipientUserId.equals(actorUserId)) {
            return;
        }
        Notification notification = notificationEventFactory.build(
                recipientUserId,
                actorUserId,
                normalizeEventTypeRequired(eventType),
                targetType,
                targetId,
                postId,
                null
        );
        notificationMapper.insert(notification);
    }

    private int toOffset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setEventType(notification.getEventType());
        response.setActorUserId(notification.getActorUserId());
        response.setActorUsername(notification.getActorUsername());
        response.setTargetType(notification.getTargetType());
        response.setTargetId(notification.getTargetId());
        response.setPostId(notification.getPostId());
        response.setRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return null;
        }
        return normalizeEventTypeRequired(eventType.trim().toUpperCase());
    }

    private String normalizeEventTypeRequired(String eventType) {
        if (!NotificationEventType.isSupported(eventType)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "eventType is invalid");
        }
        return eventType;
    }
}
