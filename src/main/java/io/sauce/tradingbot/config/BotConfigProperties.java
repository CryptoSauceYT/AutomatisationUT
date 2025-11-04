package io.sauce.tradingbot.config;

import io.sauce.tradingbot.domain.ProfileInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
@ConfigurationProperties(prefix = "bot-config")
@RefreshScope
public class BotConfigProperties {

    private Map<String, ProfileInfo> profiles = new HashMap<>();

    private Set<String> tradingPairs = new HashSet<>();

    private Long expiredTime = 6000L;

    private Long maxLeverage = 25L;

    private String tpTriggerType;

    private String slTriggerType;

}