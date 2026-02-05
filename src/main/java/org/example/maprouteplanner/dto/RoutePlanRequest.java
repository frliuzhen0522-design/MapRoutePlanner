package org.example.maprouteplanner.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 路线规划请求参数
 */
@Setter
@Getter
public class RoutePlanRequest {

    /** 起点 ID */
    private Long startPointId;

    /** 目标点 ID 列表 */
    private List<Long> targetPointIds;

    /** 规划策略：DISTANCE / TIME / COST */
    private String strategy;

    // ===== getter / setter =====

}
