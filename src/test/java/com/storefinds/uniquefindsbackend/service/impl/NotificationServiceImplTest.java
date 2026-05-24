package com.storefinds.uniquefindsbackend.service.impl;

import com.storefinds.uniquefindsbackend.entity.Notification;
import com.storefinds.uniquefindsbackend.entity.Post;
import com.storefinds.uniquefindsbackend.exception.BusinessException;
import com.storefinds.uniquefindsbackend.mapper.CommentMapper;
import com.storefinds.uniquefindsbackend.mapper.NotificationMapper;
import com.storefinds.uniquefindsbackend.mapper.PostMapper;
import com.storefinds.uniquefindsbackend.service.NotificationEventFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationEventFactory notificationEventFactory;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotificationSkipsSelfNotification() {
        notificationService.createNotification(5L, 5L, "POST_LIKED", "POST", 9L, 9L);

        verify(notificationMapper, never()).insert(org.mockito.ArgumentMatchers.any(Notification.class));
    }

    @Test
    void createNotificationPersistsRecipientActorAndTarget() {
        Notification builtNotification = new Notification();
        builtNotification.setRecipientUserId(5L);
        builtNotification.setActorUserId(8L);
        builtNotification.setEventType("COMMENT_LIKED");
        when(notificationEventFactory.build(5L, 8L, "COMMENT_LIKED", "COMMENT", 3L, 2L, null)).thenReturn(builtNotification);

        notificationService.createNotification(5L, 8L, "COMMENT_LIKED", "COMMENT", 3L, 2L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(captor.capture());
        assertEquals(5L, captor.getValue().getRecipientUserId());
        assertEquals(8L, captor.getValue().getActorUserId());
        assertEquals("COMMENT_LIKED", captor.getValue().getEventType());
    }

    @Test
    void markAsReadRejectsOtherUsersNotification() {
        Notification notification = new Notification();
        notification.setId(4L);
        notification.setRecipientUserId(9L);
        when(notificationMapper.selectById(4L)).thenReturn(notification);

        BusinessException ex = assertThrows(BusinessException.class, () -> notificationService.markAsRead(3L, 4L));

        assertEquals("notification not found", ex.getMessage());
    }

    @Test
    void getUnreadCountReturnsMapperValue() {
        when(notificationMapper.countUnreadByRecipientUserId(5L)).thenReturn(4L);

        assertEquals(4L, notificationService.getUnreadCount(5L).data().getUnreadCount());
    }

    @Test
    void notificationListIncludesDisplayMessageSummaryAndMetadata() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRecipientUserId(5L);
        notification.setActorUserId(8L);
        notification.setActorUsername("alice");
        notification.setEventType("POST_LIKED");
        notification.setTargetType("POST");
        notification.setTargetId(9L);
        notification.setPostId(9L);
        notification.setMetadata("{\"source\":\"feed\"}");
        notification.setIsRead(0);
        Post post = new Post();
        post.setId(9L);
        post.setTitle("Vintage Lamp");
        when(notificationMapper.countByRecipientUserId(5L)).thenReturn(1L);
        when(notificationMapper.selectByRecipientUserId(5L, 0, 20)).thenReturn(java.util.List.of(notification));
        when(postMapper.selectById(9L)).thenReturn(post);

        var item = notificationService.getMyNotifications(5L, null, 1, 20).data().getItems().get(0);

        assertEquals("alice liked your post.", item.getMessage());
        assertEquals("Vintage Lamp", item.getTargetSummary());
        assertEquals("feed", item.getMetadata().get("source"));
    }
}
