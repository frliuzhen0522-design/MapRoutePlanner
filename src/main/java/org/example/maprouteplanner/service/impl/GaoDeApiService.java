package org.example.maprouteplanner.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GaoDeApiService {

    private final String webKey = "你的WebKey";  // 替换成你的key
    private final RestTemplate restTemplate = new RestTemplate();

    // 获取两点之间驾车路线
    public String getDrivingRoute(double fromLng, double fromLat, double toLng, double toLat) {
        String url = String.format(
                "https://restapi.amap.com/v3/direction/driving?origin=%f,%f&destination=%f,%f&key=%s",
                fromLng, fromLat, toLng, toLat, webKey
        );
        return restTemplate.getForObject(url, String.class); // 返回JSON字符串
    }
}
