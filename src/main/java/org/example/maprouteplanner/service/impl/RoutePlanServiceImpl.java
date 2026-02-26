package org.example.maprouteplanner.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.maprouteplanner.config.GaodeConfig;
import org.example.maprouteplanner.dto.RoutePlanRequest;
import org.example.maprouteplanner.dto.RoutePlanResponse;
import org.example.maprouteplanner.dto.RouteSegment;
import org.example.maprouteplanner.mapper.PointMapper;
import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RouteAlgorithmService;
import org.example.maprouteplanner.service.RoutePlanService;
import org.example.maprouteplanner.utils.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多目标路线规划服务实现
 */
@Service
public class RoutePlanServiceImpl implements RoutePlanService {

    private static final Logger logger = LoggerFactory.getLogger(RoutePlanServiceImpl.class);

    private final PointMapper pointMapper;
    private final GaodeConfig gaodeConfig;
    private final RouteAlgorithmService routeAlgorithmService;
    private final HttpClient httpClient;

    public RoutePlanServiceImpl(
            PointMapper pointMapper,
            GaodeConfig gaodeConfig,
            RouteAlgorithmService routeAlgorithmService) {
        this.pointMapper = pointMapper;
        this.gaodeConfig = gaodeConfig;
        this.routeAlgorithmService = routeAlgorithmService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public RoutePlanResponse plan(RoutePlanRequest request) {
        // 参数校验
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (request.getStartPointId() == null) {
            throw new IllegalArgumentException("起点ID不能为空");
        }
        if (request.getTargetPointIds() == null || request.getTargetPointIds().isEmpty()) {
            throw new IllegalArgumentException("目标点列表不能为空");
        }

        // 从请求中组装起点 + 目标点的规划列表
        List<Point> points = new ArrayList<>();

        // 起点
        Point start = pointMapper.selectById(request.getStartPointId());
        if (start == null) {
            throw new IllegalArgumentException("起点不存在，ID: " + request.getStartPointId());
        }
        points.add(start);

        // 目标点
        List<Point> targets = pointMapper.selectByIds(request.getTargetPointIds());
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("未找到任何目标点");
        }
        points.addAll(targets);

        return planRoute(points);
    }

    public RoutePlanResponse planRoute(List<Point> points) {
        RoutePlanResponse response = new RoutePlanResponse();

        // 空输入直接返回
        if (points == null || points.size() < 2) {
            response.setVisitOrder(new ArrayList<>());
            response.setRoutes(new ArrayList<>());
            response.setTotalDistance(0);
            return response;
        }

        try {
            // 使用优化算法重新排序点
            List<Point> optimizedPoints = routeAlgorithmService.planRoute(points);

            List<RouteSegment> segments = new ArrayList<>();
            int totalDistance = 0;

            for (int i = 0; i < optimizedPoints.size() - 1; i++) {
                Point from = optimizedPoints.get(i);
                Point to = optimizedPoints.get(i + 1);

                // 调用高德 API 获取实际路线信息
                String json = callGaoDeApi(from, to);
                RouteSegment segment = parseRouteJson(json, from, to);

                if (segment != null) {
                    segments.add(segment);
                    totalDistance += segment.getDistance();
                } else {
                    logger.warn("无法获取 {} 到 {} 的路线信息", from.getName(), to.getName());
                }
            }

            response.setVisitOrder(
                    optimizedPoints.stream().map(Point::getId).collect(Collectors.toList())
            );
            response.setRoutes(segments);
            response.setTotalDistance(totalDistance);

            return response;

        } catch (Exception e) {
            logger.error("路线规划过程中发生错误", e);
            throw new RuntimeException("路线规划失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用高德 Web API（带 sig）
     */
    private String callGaoDeApi(Point from, Point to) {
        try {
            String webKey = gaodeConfig.getWebKey();
            String secretKey = gaodeConfig.getSecretKey();

            // 配置验证
            if (webKey == null || webKey.isBlank() || secretKey == null || secretKey.isBlank()) {
                throw new IllegalStateException("高德配置缺失，请检查 gaode.web-key 与 gaode.secret-key");
            }

            // 构建参数字符串
            String params = "origin=" + from.getLongitude() + "," + from.getLatitude() +
                    "&destination=" + to.getLongitude() + "," + to.getLatitude() +
                    "&extensions=base" +
                    "&output=JSON" +
                    "&key=" + webKey;

            // 生成签名
            String sig = SignUtils.md5(params + secretKey);

            // 构建完整URL
            String url = "https://restapi.amap.com/v3/direction/driving?" + params + "&sig=" + sig;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "MapRoutePlanner/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("高德API调用失败，状态码: {}, 响应: {}", response.statusCode(), response.body());
                return "{}";
            }

            return response.body();

        } catch (Exception e) {
            logger.error("调用高德API时发生错误: 从 {} 到 {}",
                    from.getName(), to.getName(), e);
            return "{}";
        }
    }

    /**
     * 解析路线 JSON
     */
    private RouteSegment parseRouteJson(String json, Point from, Point to) {
        try {
            if (json == null || json.trim().equals("{}")) {
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            // 检查API响应状态
            String status = root.path("status").asText();
            String info = root.path("info").asText();

            if (!"1".equals(status)) {
                logger.warn("高德API返回错误: {} - {}", status, info);
                return null;
            }

            JsonNode paths = root.path("route").path("paths");
            if (paths.isMissingNode() || paths.isEmpty()) {
                logger.warn("未找到路线信息");
                return null;
            }

            int distance = paths.get(0).path("distance").asInt(0);
            if (distance <= 0) {
                logger.warn("距离数据异常: {}", distance);
                return null;
            }

            return new RouteSegment(from.getId(), to.getId(), distance);

        } catch (Exception e) {
            logger.error("解析路线JSON时发生错误: {}", e.getMessage(), e);
            return null;
        }
    }
}
