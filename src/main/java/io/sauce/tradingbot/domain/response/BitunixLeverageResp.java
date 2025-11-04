package io.sauce.tradingbot.domain.response;

import lombok.Data;

@Data
public class BitunixLeverageResp {

    private String symbol;

    private String marginCoin;

    private Integer Leverage;

}
