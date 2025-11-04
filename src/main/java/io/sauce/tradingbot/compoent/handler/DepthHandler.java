package io.sauce.tradingbot.compoent.handler;

import io.sauce.tradingbot.compoent.provider.PriceProvider;
import io.sauce.tradingbot.domain.LastPriceItem;
import io.sauce.tradingbot.domain.response.BitunixWsResp;
import io.sauce.tradingbot.domain.response.DepthWsResp;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Component
public class DepthHandler {

    @Resource
    private PriceProvider priceProvider;

    public void handle(BitunixWsResp bitunixWsResp) {
        String jsonString = JSONUtil.toJsonString(bitunixWsResp.getData());
        DepthWsResp depth = JSONUtil.readToEntity(jsonString, DepthWsResp.class);
        BigDecimal bidPrice = null;
        if (depth.getB() != null && !depth.getB().isEmpty() && !depth.getB().get(0).isEmpty()) {
            bidPrice = depth.getB().get(0).get(0);
        } else {
            log.warn("!!receive bid price from bitunix is null: {}", jsonString);
        }
        BigDecimal askPrice = null;
        if (depth.getA() != null && !depth.getA().isEmpty() && !depth.getA().get(0).isEmpty()) {
            askPrice = depth.getA().get(0).get(0);
        }else {
            log.warn("!!receive ask price from bitunix is null: {}", jsonString);
        }
        priceProvider.setLastPrice(bitunixWsResp.getSymbol(), new LastPriceItem(bidPrice, askPrice, bitunixWsResp.getTs()));
    }

}
