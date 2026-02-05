package org.example.maprouteplanner.model;

import lombok.Data;

/**
 * Path 实体类
 * 对应数据库中的 paths 表
 * 用于描述两个兴趣点之间的路径信息
 */
@Data
public class Path {

    /**
     * 路径唯一标识
     * 对应 paths 表中的 id 字段
     */
    private Integer id;

    /**
     * 起点 POI 的 ID
     * 对应 paths 表中的 start_point_id
     */
    private Integer startPointId;

    /**
     * 终点 POI 的 ID
     * 对应 paths 表中的 end_point_id
     */
    private Integer endPointId;

    /**
     * 两点之间的距离（单位：公里）
     * 对应 paths 表中的 distance 字段
     */
    private Double distance;

    /**
     * 两点之间的预计耗时（单位：分钟）
     * 对应 paths 表中的 time 字段
     */
    private Double time;
}
