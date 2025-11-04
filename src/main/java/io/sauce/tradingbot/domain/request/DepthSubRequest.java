package io.sauce.tradingbot.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DepthSubRequest {

    private String symbol;

    private String ch;

}
