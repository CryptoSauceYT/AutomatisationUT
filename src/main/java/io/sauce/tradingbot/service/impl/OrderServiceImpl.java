package io.sauce.tradingbot.service.impl;

import io.sauce.tradingbot.compoent.handler.TpEntryCheckHandler;
import io.sauce.tradingbot.compoent.ws.BitunixApiClient;
import io.sauce.tradingbot.domain.TpInfo;
import io.sauce.tradingbot.exception.BizException;
import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.domain.LastPriceItem;
import io.sauce.tradingbot.domain.ProfileInfo;
import io.sauce.tradingbot.domain.TradingPair;
import io.sauce.tradingbot.domain.enums.ResultType;
import io.sauce.tradingbot.domain.enums.Side;
import io.sauce.tradingbot.domain.request.BitunixOperateAllRequest;
import io.sauce.tradingbot.domain.request.BitunixPlaceOrderRequest;
import io.sauce.tradingbot.domain.request.PlaceOrderRequest;
import io.sauce.tradingbot.domain.response.BitunixOrderResp;
import io.sauce.tradingbot.domain.response.BitunixPositionResp;
import io.sauce.tradingbot.domain.response.PlaceOrderResp;
import io.sauce.tradingbot.compoent.provider.CoinPairProvider;
import io.sauce.tradingbot.compoent.provider.PriceProvider;
import io.sauce.tradingbot.service.OrderService;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private BitunixApiClient bitunixApiClient;
    @Resource
    private BotConfigProperties botConfigProperties;
    @Resource
    private PriceProvider priceProvider;
    @Resource
    private CoinPairProvider coinPairProvider;
    @Resource
    private TpEntryCheckHandler tpEntryCheckHandler;

    @Override
    public PlaceOrderResp placeLimitOrder(PlaceOrderRequest request) {
        String ticker = request.getTicker().toUpperCase();
        if (ticker.contains(".")) {
            ticker = ticker.split("\\.")[0];
        }
        String profileName = request.getProfileName();
        LastPriceItem lastPrice = priceProvider.getLastPrice(ticker);
        log.info("[place order] Placing Limit Order, param: {}, lastPrice:{}", JSONUtil.toJsonString(request), JSONUtil.toJsonString(lastPrice));

        // check trading pair
        TradingPair coinPair = coinPairProvider.getCoinPair(ticker);
        if (coinPair == null) {
            log.error("[place order]CoinPair not found, profileName:{}, ticker: {}", profileName, ticker);
            throw new BizException(ResultType.TRADING_PAIR_NOT_EXISTS);
        }
        // get profile
        ProfileInfo profileInfo = botConfigProperties.getProfiles().get(profileName);
        if (profileInfo == null) {
            log.error("[place order]Profile not found, profileName: {}", profileName);
            throw new BizException(ResultType.PROFILE_NOT_EXISTS);
        }
        // check expired time
        if (isExpired(String.valueOf(request.getTimestamp().toEpochMilli()))) {
            log.error("[place order]Request already expired, profileName:{}, timestamp: {}", profileName, request.getTimestamp());
            throw new BizException(ResultType.REQUEST_ALREADY_EXPIRED);
        }
        // check lastPrice expired
        if (isExpired(lastPrice.getTimestamp())) {
            log.error("[place order]Last price already expired, profileName:{}, lastPrice: {}", profileName, JSONUtil.toJsonString(lastPrice));
            throw new BizException(ResultType.LAST_PRICE_ALREADY_EXPIRED);
        }
        // check leverage exceeds max limit
        if (profileInfo.getLeverage() > botConfigProperties.getMaxLeverage()) {
            log.error("[place order]Leverage exceeds max limit: profileName:{}, maxLeverage: {}, leverage: {}",
                    profileName, botConfigProperties.getMaxLeverage(), profileInfo.getLeverage());
            throw new BizException(ResultType.LEVERAGE_EXCEEDS_MAX_LIMIT);
        }
        // check tp is reached before entry
        Side side = Side.fromValue(request.getSide());
        if (side == null) {
            log.error("[place order]side param error, profileName:{} param:{}", profileName, request.getSide());
            throw new BizException(ResultType.SIDE_PARAM_ERROR);
        }
        Side tpSide = side.negate();
        if (isTpReached(request.getTpPrice(), tpSide, lastPrice.getLastPrice(tpSide))) {
            log.error("[place order] TP is reached before entry, profileName:{} tpPrice:{}, side:{}, lastPrice:{}",
                    profileName, request.getTpPrice(), request.getSide(), lastPrice.getLastPrice(tpSide));
            throw new BizException(ResultType.TP_IS_REACHED_BEFORE_ENTRY);
        }
        // check if current pair has open position
        if (isOpenPositionExists(ticker, profileInfo)) {
            log.info("[place order] ! {} {} exists opening positions, will close!", profileName, ticker);
            bitunixApiClient.closeAllPosition(new BitunixOperateAllRequest(ticker), profileInfo);
            log.info("[place order] ! {} {} close all positions done!", profileName, ticker);
        }
        if (isOpenOrderExists(ticker, profileInfo)) {
            log.info("[place order] ! {} {} exists opening orders, will cancel!", profileName, ticker);
            bitunixApiClient.cancelAllOrders(new BitunixOperateAllRequest(ticker), profileInfo);
            log.info("[place order] ! {} {} cancel all orders done!", profileName, ticker);
        }

        // cal tp trigger price
        BigDecimal tpTriggerPrice = calTpTriggerPrice(profileInfo, request.getTpPrice(), side);
        request.setTpTriggerPrice(tpTriggerPrice);

        BitunixPlaceOrderRequest bitunixPlaceOrderRequest = buildPlaceOrderRequest(request, profileInfo, side, coinPair, ticker);
        // place order!
        log.info("[place order] {} {} , param: {}", profileName, ticker, JSONUtil.toJsonString(bitunixPlaceOrderRequest));
        PlaceOrderResp placeOrderResp = bitunixApiClient.placeOrder(bitunixPlaceOrderRequest, profileInfo);
        if (placeOrderResp == null) {
            log.error("[place order] failed, profile: {}, ticker: {}", profileName, ticker);
            throw new BizException(ResultType.PLACE_ORDER_FAILED);
        }
        tpEntryCheckHandler.addTpInfo(new TpInfo(profileName, ticker, placeOrderResp.getOrderId(), request.getTpPrice(), tpSide, null));
        log.info("[place order] {} {} done! orderId:{}", profileName, ticker, placeOrderResp.getOrderId());
        return placeOrderResp;
    }

    private BitunixPlaceOrderRequest buildPlaceOrderRequest(PlaceOrderRequest request, ProfileInfo profileInfo, Side side, TradingPair coinPair, String ticker) {
        BigDecimal orderQty = calOrderQty(profileInfo, request.getEntryPrice(), side, coinPair);

        BitunixPlaceOrderRequest bitunixPlaceOrderRequest = new BitunixPlaceOrderRequest();
        bitunixPlaceOrderRequest.setMarginCoin("USDT");
        bitunixPlaceOrderRequest.setSymbol(ticker);
        bitunixPlaceOrderRequest.setPrice(request.getEntryPrice().stripTrailingZeros().toPlainString());
        bitunixPlaceOrderRequest.setQty(orderQty.stripTrailingZeros().toPlainString());
        bitunixPlaceOrderRequest.setSide(side.name());
        bitunixPlaceOrderRequest.setTradeSide("OPEN");
        bitunixPlaceOrderRequest.setOrderType("LIMIT");
        bitunixPlaceOrderRequest.setTpPrice(request.getTpTriggerPrice().stripTrailingZeros().toPlainString());
        bitunixPlaceOrderRequest.setTpStopType(getTriggerType(botConfigProperties.getTpTriggerType()));
        bitunixPlaceOrderRequest.setTpOrderType("LIMIT");
        bitunixPlaceOrderRequest.setTpOrderPrice(request.getTpPrice().stripTrailingZeros().toPlainString());
        bitunixPlaceOrderRequest.setSlPrice(request.getSlPrice().stripTrailingZeros().toPlainString());
        bitunixPlaceOrderRequest.setSlStopType(getTriggerType(botConfigProperties.getSlTriggerType()));
        bitunixPlaceOrderRequest.setSlOrderType("MARKET");
        return bitunixPlaceOrderRequest;
    }

    private String getTriggerType(String triggerType) {
        if ((!"LAST_PRICE".equals(triggerType) && !"MARK_PRICE".equals(triggerType))) {
            return "LAST_PRICE";
        }
        return triggerType;
    }

    /**
     * tp trigger price = request tp price * tp_offset
     *
     * @param profileInfo
     * @param tpPrice
     * @param side
     * @return
     */
    private static BigDecimal calTpTriggerPrice(ProfileInfo profileInfo, BigDecimal tpPrice, Side side) {
        BigDecimal tpOffset;
        if (side == Side.BUY) {
            // BUY, tp_offset = 1 - tp_offset
            tpOffset = BigDecimal.ONE.subtract(profileInfo.getTpOffset());
        }else {
            // SELL, tp_offset = 1 + tp_offset
            tpOffset = BigDecimal.ONE.add(profileInfo.getTpOffset());
        }
        return tpPrice.multiply(tpOffset);
    }

    /**
     * order qty = profile amount * leverage / last price
     * @param profileInfo
     * @param entryPrice
     * @param side
     * @param coinPair
     * @return
     */
    private static BigDecimal calOrderQty(ProfileInfo profileInfo, BigDecimal entryPrice, Side side, TradingPair coinPair) {
        return profileInfo.getAmount()
                .multiply(new BigDecimal(profileInfo.getLeverage()))
                .divide(entryPrice, coinPair.getBasePrecision(), RoundingMode.DOWN);
    }

    private boolean isOpenOrderExists(String ticker, ProfileInfo profileInfo) {
        BitunixOrderResp orderResp = bitunixApiClient.getOrders(ticker, profileInfo);
        if (orderResp == null) {
            throw new BizException(ResultType.API_KEY_NOT_EXISTS);
        }
        return !orderResp.getOrderList().isEmpty();
    }

    private boolean isOpenPositionExists(String ticker, ProfileInfo profileInfo) {
        List<BitunixPositionResp> positions = bitunixApiClient.getPositions(ticker, profileInfo);
        if (positions == null) {
            throw new BizException(ResultType.API_KEY_NOT_EXISTS);
        }
        return !positions.isEmpty();
    }

    private boolean isTpReached(BigDecimal tpPrice, Side side, BigDecimal lastPrice) {
        if (side == Side.BUY) {
            // buy, tp greater than last price
            return tpPrice.compareTo(lastPrice) > 0;
        }
        // sell, tp less than last price
        return tpPrice.compareTo(lastPrice) < 0;
    }


    private boolean isExpired(String timestamp) {
        long requestTime = Long.parseLong(timestamp);
        // if is seconds, convert to milliseconds
        if (timestamp.length() <= 10) {
            requestTime *= 1000;
        }
        return System.currentTimeMillis() - requestTime > botConfigProperties.getExpiredTime();
    }
}
