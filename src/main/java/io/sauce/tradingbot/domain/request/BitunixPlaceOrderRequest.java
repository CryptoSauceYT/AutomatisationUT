package io.sauce.tradingbot.domain.request;

import lombok.Data;

@Data
public class BitunixPlaceOrderRequest {

    private String marginCoin;

    private String symbol;

    private String price;

    private String qty;

    private String side;

    private String tradeSide;

    private String orderType;

    private String tpPrice;

    private String tpStopType;

    private String tpOrderType;

    private String tpOrderPrice;

    private String slPrice;

    private String slStopType;

    private String slOrderType;

    private String slOrderPrice;

}
