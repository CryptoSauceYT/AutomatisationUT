package io.sauce.tradingbot.controller;

import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.compoent.provider.CoinPairProvider;
import io.sauce.tradingbot.compoent.provider.PriceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/check")
public class CheckController {

    @Resource
    private BotConfigProperties botConfigProperties;
    @Resource
    private PriceProvider priceProvider;
    @Resource
    private CoinPairProvider coinPairProvider;

    @GetMapping("/profiles")
    public Object check() {
        log.info("check profile");
        return botConfigProperties.getProfiles();
    }

    @GetMapping("/coin_pairs")
    public Object checkCoinPair() {
        log.info("check coin pair");
        return coinPairProvider.getCoinPairs();
    }

    @GetMapping("/last_prices")
    public Object checkLastPrices() {
        log.info("check last prices");
        return priceProvider.getAllLastPrices();
    }

}