package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.InteractionEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InteractionEventMapper {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-15
     * Purpose: Insert one interaction event record for later statistics and trend calculations.
     * Params:
     * - event: interaction event entity to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(InteractionEvent event);
}
