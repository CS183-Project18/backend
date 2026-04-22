package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-17
     * Purpose: Insert a new post record.
     * Params:
     * - post: post entity to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int insert(Post post);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Query one post by primary key with author info.
     * Params:
     * - id: post id
     * Returns:
     * - Post: matched post or null
     * Throws: None
     */
    Post selectById(@Param("id") Long id);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Query all published posts for public feed display.
     * Params: None
     * Returns:
     * - List<Post>: published post list
     * Throws: None
     */
    List<Post> selectPublishedPosts();

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Query all non-deleted posts created by one user.
     * Params:
     * - userId: owner user id
     * Returns:
     * - List<Post>: owner's post list
     * Throws: None
     */
    List<Post> selectByUserId(@Param("userId") Long userId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Increase one post's view counter by one.
     * Params:
     * - id: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int increaseViewCount(@Param("id") Long id);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-20
     * Purpose: Update editable fields of one post.
     * Params:
     * - post: post entity carrying new values
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int updateById(Post post);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-21
     * Purpose: Soft delete one post owned by the specified user.
     * Params:
     * - id: post id
     * - userId: owner user id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int softDeleteById(@Param("id") Long id, @Param("userId") Long userId);
}
