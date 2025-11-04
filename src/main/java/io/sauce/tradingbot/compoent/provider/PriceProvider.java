package io.sauce.tradingbot.compoent.provider;

import io.sauce.tradingbot.domain.LastPriceItem;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceProvider {

    private final Map<String, LastPriceItem> lastPrices = new ConcurrentHashMap<>();

    public LastPriceItem getLastPrice(String coinPair) {
        return lastPrices.get(coinPair);
    }

    public void setLastPrice(String coinPair, LastPriceItem lastPrice) {
        lastPrices.put(coinPair, lastPrice);
    }

    public Map<String, LastPriceItem> getAllLastPrices() {
        return lastPrices;
    }


}
