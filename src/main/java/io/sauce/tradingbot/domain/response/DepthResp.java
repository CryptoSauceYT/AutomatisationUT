package io.sauce.tradingbot.domain.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DepthResp {

    private List<List<BigDecimal>> bids;
    private List<List<BigDecimal>> asks;

}
