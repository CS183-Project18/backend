package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Insert one new comment record.
     * Params:
     * - comment: comment entity to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(Comment comment);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Query one comment by primary key with author display info.
     * Params:
     * - id: comment id
     * Returns:
     * - Comment: matched comment or null
     * Throws: None
     */
    Comment selectById(@Param("id") Long id);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Query all visible comments under one post ordered by creation time.
     * Params:
     * - postId: target post id
     * Returns:
     * - List<Comment>: visible comment list
     * Throws: None
     */
    List<Comment> selectVisibleByPostId(@Param("postId") Long postId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Count all comments visible to frontend under one post.
     * Params:
     * - postId: target post id
     * Returns:
     * - long: matched comment count
     * Throws: None
     */
    long countDisplayableByPostId(@Param("postId") Long postId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Query one page of comments visible to frontend under one post.
     * Params:
     * - postId: target post id
     * - offset: row offset
     * - pageSize: page size
     * Returns:
     * - List<Comment>: paged comment list
     * Throws: None
     */
    List<Comment> selectDisplayableByPostId(@Param("postId") Long postId,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    long countVisibleByPostId(@Param("postId") Long postId);

    List<Comment> selectVisibleByPostIdPage(@Param("postId") Long postId,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Soft delete one comment owned by the specified user.
     * Params:
     * - id: comment id
     * - userId: comment owner user id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int softDeleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Count all comments created by one user for personal center display.
     * Params:
     * - userId: target user id
     * Returns:
     * - long: matched comment count
     * Throws: None
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Query one page of comments created by one user.
     * Params:
     * - userId: target user id
     * - offset: row offset
     * - pageSize: page size
     * Returns:
     * - List<Comment>: paged user comment list
     * Throws: None
     */
    List<Comment> selectByUserId(@Param("userId") Long userId,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Update moderation status of one comment by admin action.
     * Params:
     * - id: target comment id
     * - status: new comment status
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int updateStatusById(@Param("id") Long id, @Param("status") String status);

    int clearPinnedByPostId(@Param("postId") Long postId);

    int updatePinnedById(@Param("id") Long id, @Param("isPinned") Integer isPinned);
}
