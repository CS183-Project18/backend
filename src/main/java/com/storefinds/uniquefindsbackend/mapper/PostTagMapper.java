package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Provide post-tag relation persistence operations used by structured post metadata flows.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface PostTagMapper {

    int batchInsert(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);

    int deleteByPostId(@Param("postId") Long postId);

    List<Tag> selectTagsByPostId(@Param("postId") Long postId);

    List<Tag> selectTagsByPostIds(@Param("postIds") List<Long> postIds);
}
