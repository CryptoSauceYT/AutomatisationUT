package io.sauce.tradingbot.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class BitunixWsRequest {

    private String op;

    private Object args;

}
