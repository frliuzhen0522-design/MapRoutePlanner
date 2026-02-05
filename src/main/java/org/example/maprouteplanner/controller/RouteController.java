package org.example.maprouteplanner.controller;

import org.example.maprouteplanner.dto.RoutePlanRequest;
import org.example.maprouteplanner.dto.RoutePlanResponse;
import org.example.maprouteplanner.service.RoutePlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route")
public class RouteController {

    private final RoutePlanService routePlanService;

    public RouteController(RoutePlanService routePlanService) {
        this.routePlanService = routePlanService;
    }

    /**
     * 测试多目标路线规划接口
     */
    @PostMapping("/plan")
    public RoutePlanResponse plan(@RequestBody RoutePlanRequest request) {
        return routePlanService.plan(request);
    }
}
