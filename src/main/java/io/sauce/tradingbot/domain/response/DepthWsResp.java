package io.sauce.tradingbot.domain.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DepthWsResp {

    private List<List<BigDecimal>> b;
    private List<List<BigDecimal>> a;
}
