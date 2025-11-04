package io.sauce.tradingbot.domain;

import io.sauce.tradingbot.domain.enums.Side;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class LastPriceItem {

    private BigDecimal bidPrice;

    private BigDecimal askPrice;

    private String timestamp;

    public BigDecimal getLastPrice(Side side){
        return side == Side.BUY ? bidPrice : askPrice;
    }


}
