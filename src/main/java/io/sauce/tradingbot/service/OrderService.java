package io.sauce.tradingbot.service;

import io.sauce.tradingbot.domain.request.PlaceOrderRequest;
import io.sauce.tradingbot.domain.response.PlaceOrderResp;

public interface OrderService {

    PlaceOrderResp placeLimitOrder(PlaceOrderRequest request);

}
