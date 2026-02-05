package org.example.maprouteplanner.service.impl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.maprouteplanner.config.GaodeConfig;
import org.example.maprouteplanner.dto.RoutePlanRequest;
import org.example.maprouteplanner.dto.RoutePlanResponse;
import org.example.maprouteplanner.dto.RouteSegment;
import org.example.maprouteplanner.mapper.PointMapper;
import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RoutePlanService;
import org.example.maprouteplanner.utils.SignUtils;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多目标路线规划服务实现
 */
@Service
public class RoutePlanServiceImpl implements RoutePlanService {

    private final PointMapper pointMapper;
    private final GaodeConfig gaodeConfig;

    public RoutePlanServiceImpl(PointMapper pointMapper, GaodeConfig gaodeConfig) {
        this.pointMapper = pointMapper;
        this.gaodeConfig = gaodeConfig;
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
            String webKey = gaodeConfig.getWebKey();
            String secretKey = gaodeConfig.getSecretKey();
            // 缺失配置时直接提示，便于定位
            if (webKey == null || webKey.isBlank() || secretKey == null || secretKey.isBlank()) {
                throw new IllegalStateException("高德配置缺失，请检查 gaode.web-key 与 gaode.secret-key");
            }
            // 1️⃣ 原始参数字符串（顺序非常重要）
            String params =
                    "origin=" + from.getLongitude() + "," + from.getLatitude() +
                            "&destination=" + to.getLongitude() + "," + to.getLatitude() +
                            "&extensions=base" +
                            "&output=JSON" +
                            "&key=" + webKey;

            // 2️⃣ 生成 sig：params + 安全 key → MD5
            String sig = SignUtils.md5(params + secretKey);

            // 3️⃣ 拼最终 URL
            String url = "https://restapi.amap.com/v3/direction/driving?"
                    + params + "&sig=" + sig;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }

}
