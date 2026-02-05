package org.example.maprouteplanner.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.maprouteplanner.dto.RoutePlanRequest;
import org.example.maprouteplanner.dto.RoutePlanResponse;
import org.example.maprouteplanner.dto.RouteSegment;
import org.example.maprouteplanner.mapper.PointMapper;
import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RoutePlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多目标路线规划服务实现
 */
@Service
public class RoutePlanServiceImpl implements RoutePlanService {

    private final PointMapper pointMapper;
    private final GaoDeApiService gaoDeApiService;
    private static final Logger logger = LoggerFactory.getLogger(RoutePlanServiceImpl.class);

    public RoutePlanServiceImpl(PointMapper pointMapper, GaoDeApiService gaoDeApiService) {
        this.pointMapper = pointMapper;
        this.gaoDeApiService = gaoDeApiService;
    }

    @Override
    public RoutePlanResponse plan(RoutePlanRequest request) {
        // 从请求中组装起点 + 目标点的规划列表
        List<Point> points = new ArrayList<>();

        // 起点
        Point start = pointMapper.selectById(request.getStartPointId());
        if (start != null) {
            points.add(start);
        }

        // 目标点
        if (request.getTargetPointIds() != null && !request.getTargetPointIds().isEmpty()) {
            points.addAll(pointMapper.selectByIds(request.getTargetPointIds()));
        }

        return planRoute(points);
    }

    /**
     * 核心路线计算（暂时按顺序）
     */
    public RoutePlanResponse planRoute(List<Point> points) {
        RoutePlanResponse response = new RoutePlanResponse();
        // 空输入直接返回，避免空指针或越界
        if (points == null || points.size() < 2) {
            response.setVisitOrder(List.of());
            response.setRoutes(List.of());
            response.setTotalDistance(0);
            return response;
        }

        List<RouteSegment> segments = new ArrayList<>();
        int totalDistance = 0; // 累计总距离（米）

        for (int i = 0; i < points.size() - 1; i++) {
            Point from = points.get(i);
            Point to = points.get(i + 1);

            // 调用高德 API 获取路线信息
            String json = callGaoDeApi(from, to);
            RouteSegment segment = parseRouteJson(json, from, to);

            if (segment != null) {
                segments.add(segment);
                // 累加分段距离
                totalDistance += segment.getDistance();
            }
        }

        response.setVisitOrder(
                points.stream().map(Point::getId).collect(Collectors.toList())
        );
        response.setRoutes(segments);
        response.setTotalDistance(totalDistance);

        return response;
    }

    /**
     * ✅ 正确调用高德 Web API（带 sig）
     */
    private String callGaoDeApi(Point from, Point to) {
        try {
            return gaoDeApiService.getDrivingRoute(
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude()
            );
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("高德 API 调用失败: from={} to={}", from.getId(), to.getId(), e);
            return "{}";
        }
    }

    /**
     * 解析路线 JSON
     */
    private RouteSegment parseRouteJson(String json, Point from, Point to) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode paths = root.path("route").path("paths");
            if (paths.isMissingNode() || paths.isEmpty()) {
                return null;
            }

            int distance = paths.get(0).path("distance").asInt();
            return new RouteSegment(from.getId(), to.getId(), distance);

        } catch (Exception e) {
            logger.error("路线 JSON 解析失败: from={} to={}", from.getId(), to.getId(), e);
            return null;
        }
    }
}
