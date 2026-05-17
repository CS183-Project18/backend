package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-12
 * Purpose: Provide notification persistence operations for notification query and write flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface NotificationMapper {

    int insert(Notification notification);

    long countByRecipientUserId(@Param("recipientUserId") Long recipientUserId);

    long countUnreadByRecipientUserId(@Param("recipientUserId") Long recipientUserId);

    long countByRecipientUserIdAndEventType(@Param("recipientUserId") Long recipientUserId,
                                            @Param("eventType") String eventType);

    List<Notification> selectByRecipientUserId(@Param("recipientUserId") Long recipientUserId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    List<Notification> selectByRecipientUserIdAndEventType(@Param("recipientUserId") Long recipientUserId,
                                                           @Param("eventType") String eventType,
                                                           @Param("offset") int offset,
                                                           @Param("pageSize") int pageSize);

    Notification selectById(@Param("id") Long id);

    int markAsRead(@Param("id") Long id, @Param("recipientUserId") Long recipientUserId);

    int markAllAsRead(@Param("recipientUserId") Long recipientUserId);
}
