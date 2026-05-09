package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.ModerationLog;
import org.apache.ibatis.annotations.Mapper;

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
}
