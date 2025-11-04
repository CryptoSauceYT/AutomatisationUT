package io.sauce.tradingbot.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.BitSet;

@Data
public class PlaceOrderRequest {

    @JsonProperty("profile_name")
    private String profileName;

    @JsonProperty("long_short")
    private String side;

    private String ticker;

    private Instant timestamp;

    @JsonProperty("Entry_Price")
    private BigDecimal entryPrice;

    @JsonProperty("TP1_Price")
    private BigDecimal tpPrice;

    @JsonProperty("Stop_Price")
    private BigDecimal slPrice;

    private BigDecimal tpTriggerPrice;

}
