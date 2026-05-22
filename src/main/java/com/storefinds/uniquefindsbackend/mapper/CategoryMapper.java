package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Provide category taxonomy persistence operations for frontend queries and admin maintenance.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface CategoryMapper {

    Category selectById(@Param("id") Long id);

    List<Category> selectAll();

    List<Category> selectActive();

    List<Category> selectByParentId(@Param("parentId") Long parentId);

    int insert(Category category);

    int updateById(Category category);

    int updateActiveById(@Param("id") Long id, @Param("isActive") Integer isActive);
}
