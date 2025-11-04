package io.sauce.tradingbot.domain;

import io.sauce.tradingbot.domain.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TpInfo {

    private String profileName;

    private String coinPair;

    private String orderId;

    private BigDecimal tpPrice;

    /**
     * tp order side
     */
    private Side side;


    private BigDecimal lastPrice;

}
