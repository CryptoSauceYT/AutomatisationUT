package io.sauce.tradingbot.constants;

public class BitunixApiUrl {

    public static final String BASE_URL = "https://fapi.bitunix.com";

    public static final String GET_DEPTH = "/api/v1/futures/market/depth";

    public static final String GET_COIN_PAIR = "/api/v1/futures/market/trading_pairs";

    public static final String PLACE_ORDER = "/api/v1/futures/trade/place_order";

    public static final String CANCEL_ORDER = "/api/v1/futures/trade/cancel_all_orders";

    public static final String CLOSE_POSITION = "/api/v1/futures/trade/close_all_position";

    public static final String GET_ORDERS = "/api/v1/futures/trade/get_pending_orders";

    public static final String GET_POSITIONS = "/api/v1/futures/position/get_pending_positions";

    public static final String GET_LEVERAGE_MARGIN_MODE = "/api/v1/futures/account/get_leverage_margin_mode";

    public static final String SET_LEVERAGE = "/api/v1/futures/account/change_leverage";

}
