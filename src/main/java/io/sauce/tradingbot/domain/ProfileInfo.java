package io.sauce.tradingbot.domain;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfileInfo {

    private Integer leverage;

    private BigDecimal amount;

    private BigDecimal tpOffset;

    private String apiKey;

    private String apiSecret;

}
