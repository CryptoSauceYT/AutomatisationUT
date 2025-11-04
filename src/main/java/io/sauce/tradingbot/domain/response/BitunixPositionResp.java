package io.sauce.tradingbot.domain.response;

import lombok.Data;

@Data
public class BitunixPositionResp {

    private String positionId;

    private String symbol;

    private String qty;

    private String entryValue;

    private String side;

    private String positionMode;

    private String marginCoin;

    private String leverage;

    private String fee;

    private String realizedPNL;

    private String unrealizedPNL;

    private String liqPrice;

    private String marginRate;

    private String avgOpenPrice;

    private String ctime;

    private String mtime;

}
