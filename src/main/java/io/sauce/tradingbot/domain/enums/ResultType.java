package io.sauce.tradingbot.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultType {

    SUCCESS(0, "success"),
    TP_INIT_FAILED(1, "tp init failed"),
    PROFILE_NOT_EXISTS(100001, "profile not exists"),
    REQUEST_ALREADY_EXPIRED(100002, "request already expired"),
    LEVERAGE_EXCEEDS_MAX_LIMIT(100003, "leverage exceeds max limit: {0}"),
    TP_IS_REACHED_BEFORE_ENTRY(100004, "TP is reached before entry"),
    TRADING_PAIR_NOT_EXISTS(100005, "trading pair not exists"),
    SIDE_PARAM_ERROR(100006, "side param error"),
    API_KEY_NOT_EXISTS(100007, "api key wrong, check the profile"),
    PLACE_ORDER_FAILED(100008, "place order failed"),
    LAST_PRICE_ALREADY_EXPIRED( 100009, "last price already expired");


    private final Integer code;
    private final String message;
}
