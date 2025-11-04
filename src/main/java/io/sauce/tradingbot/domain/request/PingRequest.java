package io.sauce.tradingbot.domain.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class PingRequest{

    private String op;

    private Long ping;

}
