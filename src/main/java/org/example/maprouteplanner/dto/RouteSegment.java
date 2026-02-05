package org.example.maprouteplanner.dto;

import lombok.Getter;

/**
 * 路线中的一段（from -> to）
 */
@Getter
public class RouteSegment {

    private Long from;
    private Long to;
    private Integer distance;

    public RouteSegment(Long from, Long to, Integer distance) {
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

}
