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
}
