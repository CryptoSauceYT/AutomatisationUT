package io.sauce.tradingbot.domain.response;

import lombok.Data;

@Data
public class CoinPairResp {

    private String symbol;

    private String base;

    private String quote;

}
