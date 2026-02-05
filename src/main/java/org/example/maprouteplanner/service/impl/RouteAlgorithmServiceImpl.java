package org.example.maprouteplanner.service.impl;
import org.example.maprouteplanner.model.Point;
import org.example.maprouteplanner.service.RouteAlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class RouteAlgorithmServiceImpl implements RouteAlgorithmService {

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

            System.out.println(json); // 先打印看看返回数据
            route.add(from);
        }
        route.add(targets.get(targets.size() - 1));
        return route;
    }


    @Autowired
    private GaoDeApiService gaoDeApiService;

}