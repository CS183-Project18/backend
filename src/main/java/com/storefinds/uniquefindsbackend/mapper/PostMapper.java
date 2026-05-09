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
     * Date: 2026-05-06
     * Purpose: Count all published posts for public feed display.
     * Params: None
     * Returns:
     * - long: published post count
     * Throws: None
     */
    long countPublishedPosts();

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Query one page of published posts for public feed display.
     * Params:
     * - offset: row offset
     * - pageSize: target page size
     * Returns:
     * - List<Post>: published post page list
     * Throws: None
     */
    List<Post> selectPublishedPostsPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

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
     * Date: 2026-05-06
     * Purpose: Count all non-deleted posts created by one user.
     * Params:
     * - userId: owner user id
     * Returns:
     * - long: owner's post count
     * Throws: None
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Query one page of non-deleted posts created by one user.
     * Params:
     * - userId: owner user id
     * - offset: row offset
     * - pageSize: target page size
     * Returns:
     * - List<Post>: owner's post page list
     * Throws: None
     */
    List<Post> selectByUserIdPage(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Count all published posts matching the search conditions.
     * Params:
     * - keyword: normalized search keyword or null
     * - keywordLike: SQL like keyword or null
     * - categoryId: optional category id
     * Returns:
     * - long: matched post count
     * Throws: None
     */
    long countSearchPublishedPosts(@Param("keyword") String keyword,
                                   @Param("keywordLike") String keywordLike,
                                   @Param("categoryId") Long categoryId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Query one page of published posts matching the search conditions.
     * Params:
     * - keyword: normalized search keyword or null
     * - keywordLike: SQL like keyword or null
     * - categoryId: optional category id
     * - sort: normalized sort option
     * - offset: row offset
     * - pageSize: target page size
     * Returns:
     * - List<Post>: matched post page list
     * Throws: None
     */
    List<Post> searchPublishedPosts(@Param("keyword") String keyword,
                                    @Param("keywordLike") String keywordLike,
                                    @Param("categoryId") Long categoryId,
                                    @Param("sort") String sort,
                                    @Param("offset") int offset,
                                    @Param("pageSize") int pageSize);

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
     * Author: Enqi Guo
     * Date: 2026-05-10
     * Purpose: Increase one post's like counter by one.
     * Params:
     * - id: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int increaseLikeCount(@Param("id") Long id);

    /**
     * Author: Enqi Guo
     * Date: 2026-05-10
     * Purpose: Decrease one post's like counter by one.
     * Params:
     * - id: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int decreaseLikeCount(@Param("id") Long id);

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

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Count all posts currently pending review.
     * Params: None
     * Returns:
     * - long: pending post count
     * Throws: None
     */
    long countPendingReviewPosts();

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Query one page of posts currently pending review.
     * Params:
     * - offset: row offset
     * - pageSize: target page size
     * Returns:
     * - List<Post>: pending post page list
     * Throws: None
     */
    List<Post> selectPendingReviewPosts(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Approve one pending or hidden post and publish it.
     * Params:
     * - id: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int approveById(@Param("id") Long id);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Reject one post and save moderation reason.
     * Params:
     * - id: target post id
     * - moderationReason: moderation reason text
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int rejectById(@Param("id") Long id, @Param("moderationReason") String moderationReason);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-05-06
     * Purpose: Hide one published post and save moderation reason.
     * Params:
     * - id: target post id
     * - moderationReason: moderation reason text
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int hideById(@Param("id") Long id, @Param("moderationReason") String moderationReason);
}
