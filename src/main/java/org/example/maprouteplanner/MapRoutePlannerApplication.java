package org.example.maprouteplanner;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 * @MapperScan 用于扫描 MyBatis Mapper 接口
 */
@SpringBootApplication
@MapperScan("org.example.maprouteplanner.mapper") // 指定 Mapper 接口包
public class MapRoutePlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapRoutePlannerApplication.class, args);
    }
}
