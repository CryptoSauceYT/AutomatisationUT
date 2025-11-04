package io.sauce.tradingbot.compoent.provider;

import io.sauce.tradingbot.domain.TradingPair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoinPairProvider {

    private static final Map<String, TradingPair> coinPairsMap = new HashMap<>();

    public void refresh(List<TradingPair> coinPairs) {
        coinPairsMap.clear();
        for (TradingPair coinPair : coinPairs) {
            coinPairsMap.put(coinPair.getSymbol(), coinPair);
        }
    }

    public List<TradingPair> getCoinPairs() {
        return new ArrayList<>(coinPairsMap.values());
    }

    public TradingPair getCoinPair(String coinPair) {
        return coinPairsMap.get(coinPair);
    }

}
