package org.example.maprouteplanner.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.maprouteplanner.model.Point;

import java.util.List;

/**
 * PointMapper
 * 用于定义对 points 表的数据库操作方法
 *
 * 注意：
 * 这里只“声明方法”，不写 SQL
 * SQL 会写在对应的 PointMapper.xml 中
 */
@Mapper
public interface PointMapper {
    /**
     * 查询所有 POI 点
     */
    List<Point> findAll();
    // 根据 ID 查询单个点
    Point selectById(@Param("id") Long id);

    // 根据 ID 列表查询多个点
    List<Point> selectByIds(@Param("ids") List<Long> ids);
}
