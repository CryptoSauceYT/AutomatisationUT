package io.sauce.tradingbot.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BitunixOperateAllRequest {

    private String symbol;
}
