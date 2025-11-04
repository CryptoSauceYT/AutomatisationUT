package io.sauce.tradingbot.domain;

import lombok.Data;

@Data
public class TradingPair {

    private String symbol;

    private Integer basePrecision;

    private Integer quotePrecision;

}
