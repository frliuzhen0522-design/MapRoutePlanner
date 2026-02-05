package org.example.maprouteplanner.controller;

import org.example.maprouteplanner.mapper.PointMapper;
import org.example.maprouteplanner.model.Point;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * PointController
 * 用于对外提供 POI 数据接口
 */
@RestController
public class PointController {

    private final PointMapper pointMapper;

    // 构造器注入（推荐方式）
    public PointController(PointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    /**
     * 查询所有 POI
     * 访问地址：http://localhost:8080/points
     */
    @GetMapping("/points")
    public List<Point> getAllPoints() {
        return pointMapper.findAll();
    }
}
