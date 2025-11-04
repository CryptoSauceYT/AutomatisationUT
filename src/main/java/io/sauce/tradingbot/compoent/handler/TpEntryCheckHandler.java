package io.sauce.tradingbot.compoent.handler;

import io.sauce.tradingbot.compoent.provider.PriceProvider;
import io.sauce.tradingbot.compoent.ws.BitunixApiClient;
import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.domain.LastPriceItem;
import io.sauce.tradingbot.domain.ProfileInfo;
import io.sauce.tradingbot.domain.TpInfo;
import io.sauce.tradingbot.domain.enums.ResultType;
import io.sauce.tradingbot.domain.enums.Side;
import io.sauce.tradingbot.domain.request.BitunixOperateAllRequest;
import io.sauce.tradingbot.domain.response.BitunixOrderResp;
import io.sauce.tradingbot.exception.BizException;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TpEntryCheckHandler {

    private Set<TpInfo> tpInfoSet = new HashSet<>();
    @Resource
    private BotConfigProperties botConfigProperties;
    @Resource
    private BitunixApiClient bitunixApiClient;
    @Resource
    private PriceProvider priceProvider;

    private Boolean isInit = false;

    public void addTpInfo(TpInfo tpInfo) {
        tpInfoSet.add(tpInfo);
        log.info("[tp entry check handler] add tpInfo: {}", JSONUtil.toJsonString(tpInfo));
    }

    public void batchAddTpInfo(List<TpInfo> tpInfoList) {
        tpInfoSet.addAll(tpInfoList);
        log.info("[tp entry check handler] batch add tpInfo: {}", JSONUtil.toJsonString(tpInfoList));
    }

    public void checkTpEntry() {
        if (!isInit) {
            loadAllTpInfo();
            isInit = true;
            log.info("[init] load tp info done!");
        }

        List<TpInfo> tpInfos = tpInfoSet.stream()
                .filter(tpInfo -> {
                    LastPriceItem lastPriceItem = priceProvider.getLastPrice(tpInfo.getCoinPair());
                    if (lastPriceItem == null) {
                        log.error("[tp reached before entry check]LastPrice not found, coinPair: {}", tpInfo.getCoinPair());
                        return false;
                    }
                    BigDecimal lastPrice = lastPriceItem.getLastPrice(tpInfo.getSide().negate());
                    boolean tpReachBeforeEntry = isTpReachBeforeEntry(tpInfo.getTpPrice(), tpInfo.getSide(), lastPrice);
                    if (tpReachBeforeEntry) {
                        tpInfo.setLastPrice(lastPrice);
                    }
                    return tpReachBeforeEntry;
                })
                .collect(Collectors.toList());
        if (tpInfos.isEmpty()) {
            return;
        }

        // check is order filled
        Map<String, List<TpInfo>> tpInfoMap = tpInfos.stream().collect(Collectors.groupingBy(TpInfo::getProfileName));
        for (String profileName : tpInfoMap.keySet()) {
            ProfileInfo profileInfo = botConfigProperties.getProfiles().get(profileName);
            List<TpInfo> cTpInfos = tpInfoMap.get(profileName);
            for (TpInfo cTpInfo : cTpInfos) {
                // check order filled
                BitunixOrderResp orders = bitunixApiClient.getOrders(cTpInfo.getCoinPair(), profileInfo);
                if (orders == null || orders.getTotal() == 0) {
                    continue;
                }
                // if the original order exits, cancel all orders
                if (checkOriginOrderExists(cTpInfo, orders)) {
                    // check again
                    orders = bitunixApiClient.getOrders(cTpInfo.getCoinPair(), profileInfo);
                    if (orders.getTotal() == 0) {
                        continue;
                    }
                    boolean needCancel = checkOriginOrderExists(cTpInfo, orders);
                    if (!needCancel){
                        continue;
                    }
                    log.info("[TP Reach Before Entry], tp info:{}, lastPrice:{}", JSONUtil.toJsonString(cTpInfo), cTpInfo.getLastPrice());
                    // cancel All orders
                    bitunixApiClient.cancelAllOrders(new BitunixOperateAllRequest(cTpInfo.getCoinPair()), profileInfo);
                }
            }
        }
        // remove from tpInfoSet
        tpInfoSet.removeIf(tpInfo -> tpInfoMap.containsKey(tpInfo.getProfileName()));
    }

    private static boolean checkOriginOrderExists(TpInfo cTpInfo, BitunixOrderResp orders) {
        return orders.getOrderList().stream().anyMatch(order -> order.getOrderId().equals(cTpInfo.getOrderId()));
    }

    private void loadAllTpInfo() {
        Set<Map.Entry<String, ProfileInfo>> entries = botConfigProperties.getProfiles().entrySet();
        for (Map.Entry<String, ProfileInfo> entry : entries) {
            ProfileInfo profileInfo = entry.getValue();
            try {
                BitunixOrderResp resp = bitunixApiClient.getOrders("", profileInfo);
                if (resp == null) {
                    throw new BizException(ResultType.TP_INIT_FAILED);
                }
                List<TpInfo> tpInfos = resp.getOrderList().stream().filter(bitunixOrder -> bitunixOrder.getTpPrice() != null).map(bitunixOrder -> new TpInfo(entry.getKey(),
                        bitunixOrder.getSymbol(),
                        bitunixOrder.getOrderId(),
                        new BigDecimal(bitunixOrder.getTpPrice()),
                        // reverse side
                        bitunixOrder.getSide().equals("BUY") ? Side.SELL : Side.BUY,
                        null
                )).collect(Collectors.toList());
                if (!tpInfos.isEmpty()) {
                    batchAddTpInfo(tpInfos);
                }
            }catch (Exception e){
                log.error("load tp info error!, profileInfo:{}", JSONUtil.toJsonString(entry), e);
            }
        }
    }

    private boolean isTpReachBeforeEntry(BigDecimal tpPrice, Side side, BigDecimal lastPrice) {
        if (side == Side.BUY) {
            // buy, tp  greater than last price
            return tpPrice.compareTo(lastPrice) > 0;
        }
        // sell, tp  less than last price
        return tpPrice.compareTo(lastPrice) < 0;
    }

}
