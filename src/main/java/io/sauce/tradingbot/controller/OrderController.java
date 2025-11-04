package io.sauce.tradingbot.controller;

import io.sauce.tradingbot.domain.CommonResult;
import io.sauce.tradingbot.domain.request.PlaceOrderRequest;
import io.sauce.tradingbot.domain.response.PlaceOrderResp;
import io.sauce.tradingbot.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/place_limit_order")
    public CommonResult<String> placeLimitOrder(@RequestBody @Validated PlaceOrderRequest request) {
        PlaceOrderResp placeOrderResp = orderService.placeLimitOrder(request);
        return new CommonResult<>(placeOrderResp.getOrderId());
    }

}
