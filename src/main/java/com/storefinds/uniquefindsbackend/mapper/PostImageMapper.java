package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.PostImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostImageMapper {

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Batch insert image records for one post.
     * Params:
     * - images: post image entities to persist
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int batchInsert(@Param("images") List<PostImage> images);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-18
     * Purpose: Query all images of one post ordered by sort field.
     * Params:
     * - postId: target post id
     * Returns:
     * - List<PostImage>: ordered image list
     * Throws: None
     */
    List<PostImage> selectByPostId(@Param("postId") Long postId);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Query all images belonging to a set of posts.
     * Params:
     * - postIds: target post ids
     * Returns:
     * - List<PostImage>: all matched images
     * Throws: None
     */
    List<PostImage> selectByPostIds(@Param("postIds") List<Long> postIds);

    /**
     * Author: Kaijie Zhu
     * Date: 2026-04-19
     * Purpose: Delete all images bound to one post before replacement.
     * Params:
     * - postId: target post id
     * Returns:
     * - int: affected rows
     * Throws: None
     */
    int deleteByPostId(@Param("postId") Long postId);
}
