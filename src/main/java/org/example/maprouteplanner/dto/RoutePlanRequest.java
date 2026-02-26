package org.example.maprouteplanner.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 路线规划请求参数
 */
@Setter
@Getter
public class RoutePlanRequest {

    /** 起点 ID */
    @NotNull(message = "起点ID不能为空")
    private Long startPointId;

    /** 目标点 ID 列表 */
    @NotNull(message = "目标点列表不能为空")
    private List<Long> targetPointIds;

    /** 规划策略：DISTANCE / TIME / COST */
    private String strategy = "DISTANCE";

    // 构造函数
    public RoutePlanRequest() {}

    public RoutePlanRequest(Long startPointId, List<Long> targetPointIds) {
        this.startPointId = startPointId;
        this.targetPointIds = targetPointIds;
    }
}
