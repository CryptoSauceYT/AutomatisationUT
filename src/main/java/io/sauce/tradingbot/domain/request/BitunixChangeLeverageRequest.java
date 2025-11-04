package io.sauce.tradingbot.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BitunixChangeLeverageRequest {

    private String symbol;

    private String marginCoin;

    private String leverage;

}
