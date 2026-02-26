package org.example.maprouteplanner.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Point {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;

    // 添加toString方法便于调试
    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
