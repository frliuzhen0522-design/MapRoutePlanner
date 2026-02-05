package org.example.maprouteplanner.service.impl;

import org.example.maprouteplanner.config.GaodeConfig;
import org.example.maprouteplanner.utils.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GaoDeApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final GaodeConfig gaodeConfig;
    private static final Logger logger = LoggerFactory.getLogger(GaoDeApiService.class);

    public GaoDeApiService(GaodeConfig gaodeConfig) {
        this.gaodeConfig = gaodeConfig;
    }

    // 获取两点之间驾车路线
    public String getDrivingRoute(double fromLng, double fromLat, double toLng, double toLat) {
        String webKey = gaodeConfig.getWebKey();
        String secretKey = gaodeConfig.getSecretKey();
        // 配置缺失时尽早抛错，避免发出无效请求
        if (webKey == null || webKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("高德配置缺失，请检查 gaode.web-key 与 gaode.secret-key");
        }
        String params = "origin=" + fromLng + "," + fromLat
                + "&destination=" + toLng + "," + toLat
                + "&extensions=base"
                + "&output=JSON"
                + "&key=" + webKey;
        String sig = SignUtils.md5(params + secretKey);
        String url = "https://restapi.amap.com/v3/direction/driving?" + params + "&sig=" + sig;
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            logger.warn("高德 API 返回空响应: origin={},{} destination={},{}", fromLng, fromLat, toLng, toLat);
            return "{}";
        }
        return response; // 返回JSON字符串
    }
}
