package io.sauce.tradingbot.domain.response;

import lombok.Data;

@Data
public class BitunixWsResp {

    private String op;

    private String ch;

    private String symbol;

    private String ts;

    private Object data;

}
