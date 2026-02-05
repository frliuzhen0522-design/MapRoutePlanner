package org.example.maprouteplanner.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 路线规划返回结果
 */
@Setter
@Getter
public class RoutePlanResponse {

    /** 总距离（单位：米） */
    private Integer totalDistance;

    /** 实际访问顺序 */
    private List<Long> visitOrder;

    /** 每一段路径信息 */
    private List<RouteSegment> routes;

    // ===== getter / setter =====

}
