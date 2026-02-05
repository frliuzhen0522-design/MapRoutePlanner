package org.example.maprouteplanner.service.impl;

import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RouteAlgorithmService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteAlgorithmServiceImpl implements RouteAlgorithmService {

    private final GaoDeApiService gaoDeApiService;

    public RouteAlgorithmServiceImpl(GaoDeApiService gaoDeApiService) {
        this.gaoDeApiService = gaoDeApiService;
    }

    @Override
    public List<Point> planRoute(List<Point> targets) {
        List<Point> route = new ArrayList<>();
        // 空列表直接返回，避免后续越界
        if (targets == null || targets.isEmpty()) {
            return route;
        }

        for (int i = 0; i < targets.size() - 1; i++) {
            Point from = targets.get(i);
            Point to = targets.get(i + 1);

            // 调用高德API
            String json = gaoDeApiService.getDrivingRoute(
                    from.getLongitude(), from.getLatitude(),
                    to.getLongitude(), to.getLatitude()
            );
            // TODO: 根据返回结果进行距离计算或优化策略
            route.add(from);
        }
        route.add(targets.get(targets.size() - 1));
        return route;
    }
}
