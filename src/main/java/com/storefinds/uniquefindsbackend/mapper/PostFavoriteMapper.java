package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostFavoriteMapper {

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Insert one favorite relation and ignore duplicate favorites.
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
     * Purpose: Delete one favorite relation if it exists.
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
     * Purpose: Check whether one user currently favorites one post.
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
     * Purpose: Batch query favorited post ids for one user.
     * Params:
     * - userId: actor user id
     * - postIds: target post id list
     * Returns:
     * - List<Long>: favorited post ids
     * Throws: None
     */
    List<Long> selectFavoritedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * Author: Enqi Guo
     * Date: 2026-04-27
     * Purpose: Query all visible favorited posts for one user ordered by favorite time.
     * Params:
     * - userId: actor user id
     * Returns:
     * - List<Post>: favorited post list
     * Throws: None
     */
    List<Post> selectFavoritePostsByUserId(@Param("userId") Long userId);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Count all visible favorited posts of one user.
     * Params:
     * - userId: actor user id
     * Returns:
     * - long: favorited post count
     * Throws: None
     */
    long countFavoritePostsByUserId(@Param("userId") Long userId);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-06
     * Purpose: Query one page of visible favorited posts of one user ordered by favorite time.
     * Params:
     * - userId: actor user id
     * - offset: row offset
     * - pageSize: target page size
     * Returns:
     * - List<Post>: favorited post page list
     * Throws: None
     */
    List<Post> selectFavoritePostsByUserIdPage(@Param("userId") Long userId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);
}
