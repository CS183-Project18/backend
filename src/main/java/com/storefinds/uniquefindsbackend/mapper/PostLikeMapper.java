package com.storefinds.uniquefindsbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostLikeMapper {

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Insert one like relation and ignore duplicate likes.
     * Params:
     * - userId: actor user id
     * - postId: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insertIgnore(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Delete one like relation if it exists.
     * Params:
     * - userId: actor user id
     * - postId: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Check whether one user currently likes one post.
     * Params:
     * - userId: actor user id
     * - postId: target post id
     * Returns:
     * - int: relation count
     * Throws: None
     */
    int countByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Batch query liked post ids for one user.
     * Params:
     * - userId: actor user id
     * - postIds: target post id list
     * Returns:
     * - List<Long>: liked post ids
     * Throws: None
     */
    List<Long> selectLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
