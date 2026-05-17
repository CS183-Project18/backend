package com.storefinds.uniquefindsbackend.controller.user;

import com.storefinds.uniquefindsbackend.common.Result;
import com.storefinds.uniquefindsbackend.dto.NotificationResponse;
import com.storefinds.uniquefindsbackend.dto.PageResponse;
import com.storefinds.uniquefindsbackend.dto.UnreadNotificationCountResponse;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.security.CurrentUser;
import com.storefinds.uniquefindsbackend.service.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/notifications")
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-12
 * Purpose: Provide notification query and read-state endpoints for the current user.
 * Params: None
 * Returns: None
 * Throws: None
 */
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Inject notification service for notification endpoints.
     * Params:
     * - notificationService: notification business service
     * Returns: None
     * Throws: None
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-12
     * Purpose: Query one page of notifications owned by the current authenticated user.
     * Params:
     * - page: requested page number
     * - pageSize: requested page size
     * - authentication: spring authentication object
     * Returns:
     * - Result<PageResponse<NotificationResponse>>: notification page
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<PageResponse<NotificationResponse>> getMyNotifications(@RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be greater than 0") Integer page,
                                                                         @RequestParam(required = false) String eventType,
                                                                         @RequestParam(defaultValue = "20") @Min(value = 1, message = "pageSize must be greater than 0") @Max(value = 100, message = "pageSize must be less than or equal to 100") Integer pageSize,
                                                                         Authentication authentication) {
        return notificationService.getMyNotifications(requireCurrentUser(authentication).userId(), eventType, page, pageSize);
    }

    @GetMapping("/unread-count")
    public Result<UnreadNotificationCountResponse> getUnreadCount(Authentication authentication) {
        return notificationService.getUnreadCount(requireCurrentUser(authentication).userId());
    }

    @PostMapping("/{notificationId}/read")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Mark one notification as read for the current authenticated user.
     * Params:
     * - notificationId: target notification id
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated or notification is inaccessible
     */
    public Result<Void> markAsRead(@PathVariable @Min(value = 1, message = "notificationId must be greater than 0") Long notificationId,
                                   Authentication authentication) {
        return notificationService.markAsRead(requireCurrentUser(authentication).userId(), notificationId);
    }

    @PostMapping("/read-all")
    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-13
     * Purpose: Mark all notifications owned by the current authenticated user as read.
     * Params:
     * - authentication: spring authentication object
     * Returns:
     * - Result<Void>: operation result
     * Throws:
     * - BusinessException: when current request is unauthenticated
     */
    public Result<Void> markAllAsRead(Authentication authentication) {
        return notificationService.markAllAsRead(requireCurrentUser(authentication).userId());
    }

    private CurrentUser requireCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            throw new BusinessException("unauthorized");
        }
        return currentUser;
    }
}
