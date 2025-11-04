package io.sauce.tradingbot.domain.response;

import lombok.Data;

import java.util.List;

/**
 * {
 *         "orderId": "11111",
 *         "qty": "1",
 *         "tradeQty": "0.5",
 *         "price": "60000",
 *         "symbol": "BTCUSDT",
 *         "positionMode": "HEDGE",
 *         "marginMode": "ISOLATION",
 *         "leverage": 15,
 *         "status": "NEW",
 *         "fee": "0.01",
 *         "realizedPNL": "1.78",
 *         "type": "LIMIT",
 *         "effect": "GTC",
 *         "reduceOnly": false,
 *         "clientId": "22222",
 *         "tpPrice": "61000",
 *         "tpStopType": "MARK",
 *         "tpOrderType": "LIMIT",
 *         "tpOrderPrice": "61000.1",
 *         "slPrice": "59000",
 *         "slStopType": "MARK",
 *         "slOrderType": "LIMIT",
 *         "slOrderPrice": "59000.1",
 *         "source": "api",
 *         "ctime": 1597026383085,
 *         "mtime": 1597026383085
 *       }
 */
@Data
public class BitunixOrderResp {

    private Integer total;

    private List<BitunixOrder> orderList;

    @Data
    public static class BitunixOrder {

        private String orderId;
        private String symbol;
        private String qty;
        private String tradeQty;
        private String positionMode;
        private Integer leverage;
        private String price;
        private String side;
        private String orderType;
        private String effect;
        private String reduceOnly;
        private String status;
        private String fee;
        private String realizedPNL;
        private String tpPrice;
        private String tpStopType;
        private String tpOrderType;
        private String tpOrderPrice;
        private String slPrice;
        private String slStopType;
        private String slOrderType;
        private String slOrderPrice;
        private String ctime;
        private String mtime;

    }

}
