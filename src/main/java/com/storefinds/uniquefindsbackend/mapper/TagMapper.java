package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Provide tag dictionary persistence operations for tag management and post tag validation.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface TagMapper {

    Tag selectById(@Param("id") Long id);

    Tag selectByName(@Param("name") String name);

    List<Tag> selectAll();

    List<Tag> selectByIds(@Param("ids") List<Long> ids);

    int insert(Tag tag);

    int updateById(Tag tag);
}
