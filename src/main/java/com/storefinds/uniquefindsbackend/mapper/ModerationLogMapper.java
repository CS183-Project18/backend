package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.ModerationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ModerationLogMapper {

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Insert one moderation log record.
     * Params:
     * - moderationLog: moderation log entity to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(ModerationLog moderationLog);

    /**
 * Author: Enqi Guo
     * Date: 2026-05-22
     * Purpose: Count moderation audit logs matching optional admin filters.
     * Params:
     * - targetType: optional target type
     * - targetId: optional target id
     * - moderatorId: optional moderator id
     * - action: optional moderation action
     * - startTime: optional created-at lower bound
     * - endTime: optional created-at upper bound
     * Returns:
     * - long: matched row count
     * Throws: None
     */
    long countByFilter(@Param("targetType") String targetType,
                       @Param("targetId") Long targetId,
                       @Param("moderatorId") Long moderatorId,
                       @Param("action") String action,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-22
     * Purpose: Query moderation audit logs matching optional admin filters.
     * Params:
     * - targetType: optional target type
     * - targetId: optional target id
     * - moderatorId: optional moderator id
     * - action: optional moderation action
     * - startTime: optional created-at lower bound
     * - endTime: optional created-at upper bound
     * - offset: pagination offset
     * - pageSize: pagination size
     * Returns:
     * - List<ModerationLog>: matched moderation logs
     * Throws: None
     */
    List<ModerationLog> selectByFilter(@Param("targetType") String targetType,
                                       @Param("targetId") Long targetId,
                                       @Param("moderatorId") Long moderatorId,
                                       @Param("action") String action,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);
}
