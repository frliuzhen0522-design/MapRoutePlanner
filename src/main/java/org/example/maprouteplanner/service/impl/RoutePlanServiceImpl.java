package org.example.maprouteplanner.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.maprouteplanner.dto.RoutePlanRequest;
import org.example.maprouteplanner.dto.RoutePlanResponse;
import org.example.maprouteplanner.dto.RouteSegment;
import org.example.maprouteplanner.mapper.PointMapper;
import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RoutePlanService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 多目标路线规划服务实现
 */
@Service
public class RoutePlanServiceImpl implements RoutePlanService {

    private final PointMapper pointMapper;

    // ⚠️ 建议后面放到 application.yml，这里先写死方便测试
    private static final String GAODE_WEB_KEY = "8cb054591f58e456f5ac66ff5d3a7e9d";
    private static final String GAODE_SECURITY_KEY = "ab9fbe2812ffcce65129168e9cbb249f";

    public RoutePlanServiceImpl(PointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    @Override
    public RoutePlanResponse plan(RoutePlanRequest request) {

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

        List<RouteSegment> segments = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            Point from = points.get(i);
            Point to = points.get(i + 1);

            String json = callGaoDeApi(from, to);
            RouteSegment segment = parseRouteJson(json, from, to);

            if (segment != null) {
                segments.add(segment);
            }
        }

        RoutePlanResponse response = new RoutePlanResponse();
        response.setVisitOrder(
                points.stream().map(Point::getId).collect(Collectors.toList())
        );
        response.setRoutes(segments);

        return response;
    }

    /**
     * ✅ 正确调用高德 Web API（带 sig）
     */
    private String callGaoDeApi(Point from, Point to) {
        try {
            // 1️⃣ 原始参数字符串（顺序非常重要）
            String params =
                    "origin=" + from.getLongitude() + "," + from.getLatitude() +
                            "&destination=" + to.getLongitude() + "," + to.getLatitude() +
                            "&extensions=base" +
                            "&output=JSON" +
                            "&key=" + GAODE_WEB_KEY;

            // 2️⃣ 生成 sig：params + 安全 key → MD5
            String sig = md5(params + GAODE_SECURITY_KEY);

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

    /**
     * MD5 工具方法
     */
    private String md5(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(text.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
