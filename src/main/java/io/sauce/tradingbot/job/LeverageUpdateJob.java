package io.sauce.tradingbot.job;

import io.sauce.tradingbot.compoent.ws.BitunixApiClient;
import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.domain.ProfileInfo;
import io.sauce.tradingbot.domain.request.BitunixChangeLeverageRequest;
import io.sauce.tradingbot.domain.response.BitunixLeverageResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LeverageUpdateJob {

    @Resource
    private BitunixApiClient bitunixApiClient;
    @Resource
    private BotConfigProperties botConfigProperties;

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 20, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Scheduled(fixedRate = 10000)
    public void updateLeverage() {
        Collection<ProfileInfo> values = botConfigProperties.getProfiles().values();
        Set<String> tradingPairs = botConfigProperties.getTradingPairs();
//        List<Future> futures = new ArrayList<>();
        for (ProfileInfo profileInfo : values) {
            checkAndUpdate(profileInfo, tradingPairs);
        }
//        futures.forEach(future -> {
//            try {
//                future.get();
//            } catch (Exception e) {
//                log.error("update leverage error", e);
//            }
//        });
    }

    private void checkAndUpdate(ProfileInfo profileInfo, Set<String> tradingPairs) {
        for (String tradingPair : tradingPairs) {
            BitunixLeverageResp leverageResp = bitunixApiClient.getLeverage(tradingPair, profileInfo);
            if (leverageResp.getLeverage().compareTo(profileInfo.getLeverage()) != 0) {
                bitunixApiClient.modifyLeverage(new BitunixChangeLeverageRequest(tradingPair, leverageResp.getMarginCoin(), profileInfo.getLeverage().toString()), profileInfo);
            }
        }
    }

}
