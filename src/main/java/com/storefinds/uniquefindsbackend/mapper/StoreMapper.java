package com.storefinds.uniquefindsbackend.mapper;

import com.storefinds.uniquefindsbackend.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
/**
 * Author: Kaijie Zhu
 * Date: 2026-05-18
 * Purpose: Provide store persistence operations for structured content management and public discovery.
 * Params: None
 * Returns: None
 * Throws: None
 */
public interface StoreMapper {

    Store selectById(@Param("id") Long id);

    List<Store> selectAll();

    List<Store> selectPublic();

    int insert(Store store);

    int updateById(Store store);

    int updateStatusById(@Param("id") Long id, @Param("status") String status);
}
