package io.sauce.tradingbot.domain.response;

import lombok.Data;

import java.util.List;

@Data
public class CancelAllOrderResp {

    List<Object> successList;

    List<Object> failureList;

}
