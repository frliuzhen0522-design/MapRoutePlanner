package org.example.maprouteplanner.service;

import org.example.maprouteplanner.model.Point;

import java.util.List;

public interface RouteAlgorithmService {
    // 获取多目标路线规划
    List<Point> planRoute(List<Point> targets);
}
