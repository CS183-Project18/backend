package com.storefinds.uniquefindsbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-11
 * Purpose: Provide comment-like relation persistence operations used by comment interaction logic.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface CommentLikeMapper {

    int insertIgnore(@Param("userId") Long userId, @Param("commentId") Long commentId);

    int deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    int countByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    List<Long> selectLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);
}
