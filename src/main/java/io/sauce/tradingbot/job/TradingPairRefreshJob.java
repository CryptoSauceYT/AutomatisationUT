package io.sauce.tradingbot.job;

import io.sauce.tradingbot.compoent.ws.BitunixApiClient;
import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.domain.TradingPair;
import io.sauce.tradingbot.compoent.provider.CoinPairProvider;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TradingPairRefreshJob {

    @Resource
    private BitunixApiClient bitunixApiClient;
    @Resource
    private BotConfigProperties botConfigProperties;
    @Resource
    private CoinPairProvider coinPairProvider;

    @Scheduled(fixedRate = 3000)
    public void refresh() {
        Set<String> tradingPairs = botConfigProperties.getTradingPairs();
        List<TradingPair> coinPairs = bitunixApiClient.getCoinPairs(tradingPairs);
        if (coinPairs.size() != tradingPairs.size()) {
            tradingPairs.removeAll(coinPairs.stream().map(TradingPair::getSymbol).collect(Collectors.toSet()));
            log.warn("coinPairs size is not equal to tradingPairs size. diff: {}", JSONUtil.toJsonString(tradingPairs));
        }
        coinPairProvider.refresh(coinPairs);
    }

}
