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
    private Integer totalDistance = 0;

    /** 实际访问顺序 */
    private List<Long> visitOrder;

    /** 每一段路径信息 */
    private List<RouteSegment> routes;

    // 构造函数
    public RoutePlanResponse() {}

    public RoutePlanResponse(Integer totalDistance, List<Long> visitOrder, List<RouteSegment> routes) {
        this.totalDistance = totalDistance;
        this.visitOrder = visitOrder;
        this.routes = routes;
    }

    @Override
    public String toString() {
        return "RoutePlanResponse{" +
                "totalDistance=" + totalDistance +
                ", visitOrder=" + visitOrder +
                ", routes数量=" + (routes != null ? routes.size() : 0) +
                '}';
    }
}
