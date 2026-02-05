package org.example.maprouteplanner.service;
import org.example.maprouteplanner.dto.RoutePlanRequest;
import org.example.maprouteplanner.dto.RoutePlanResponse;
import org.example.maprouteplanner.model.Point;
import java.util.List;

public interface RoutePlanService {
    RoutePlanResponse planRoute(List<Point> targets);

    RoutePlanResponse plan(RoutePlanRequest request);
}
