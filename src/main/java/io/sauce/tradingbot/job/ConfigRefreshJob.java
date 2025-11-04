package io.sauce.tradingbot.job;

import org.springframework.http.RequestEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Component
public class ConfigRefreshJob {

    @Resource
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 2000)
    public void refresh() {
        RequestEntity<String> request = RequestEntity.post("http://localhost:8080/actuator/refresh")
                .header("Content-Type", "application/json")
                .body("{}");
        restTemplate.exchange(request, String.class);
    }

}
