package io.sauce.tradingbot.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Side {

    BUY("long"),
    SELL("short");

    private final String value;

    public static Side fromValue(String value) {
        for (Side side : values()) {
            if (side.value.equals(value)) {
                return side;
            }
        }
        return null;
    }

    public Side negate(){
        return this == BUY ? SELL : BUY;
    }

}
