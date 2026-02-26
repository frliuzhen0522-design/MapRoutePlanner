package org.example.maprouteplanner.service.impl;

import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RouteAlgorithmService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteAlgorithmServiceImpl implements RouteAlgorithmService {

    @Override
    public List<Point> planRoute(List<Point> targets) {
        // 空列表直接返回
        if (targets == null || targets.isEmpty()) {
            return new ArrayList<>();
        }

        // 如果只有一个点，直接返回
        if (targets.size() == 1) {
            return new ArrayList<>(targets);
        }

        // 使用最近邻算法进行路径规划
        return nearestNeighborAlgorithm(new ArrayList<>(targets));
    }

    /**
     * 最近邻算法实现TSP近似解
     * 时间复杂度: O(n²)
     */
    private List<Point> nearestNeighborAlgorithm(List<Point> points) {
        List<Point> result = new ArrayList<>();
        Set<Point> unvisited = new HashSet<>(points);

        // 选择第一个点作为起始点
        Point current = points.get(0);
        result.add(current);
        unvisited.remove(current);

        // 贪心选择最近的未访问点
        while (!unvisited.isEmpty()) {
            Point nearest = findNearestPoint(current, unvisited);
            result.add(nearest);
            unvisited.remove(nearest);
            current = nearest;
        }

        return result;
    }

    /**
     * 找到距离当前点最近的未访问点
     */
    private Point findNearestPoint(Point current, Set<Point> candidates) {
        Point nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point candidate : candidates) {
            double distance = calculateDistance(current, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }

        return nearest;
    }

    /**
     * 计算两点间直线距离（简化版）
     * 实际应用中应该调用高德API获取真实道路距离
     */
    private double calculateDistance(Point p1, Point p2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        // Haversine公式计算球面距离
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.pow(Math.sin(dlat/2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));

        // 地球半径（千米）
        double radius = 6371.0;
        return radius * c;
    }
}
