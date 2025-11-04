package io.sauce.tradingbot.compoent.ws;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.sauce.tradingbot.compoent.SignatureGenerator;
import io.sauce.tradingbot.constants.BitunixApiUrl;
import io.sauce.tradingbot.domain.ProfileInfo;
import io.sauce.tradingbot.domain.TradingPair;
import io.sauce.tradingbot.domain.request.BitunixChangeLeverageRequest;
import io.sauce.tradingbot.domain.request.BitunixPlaceOrderRequest;
import io.sauce.tradingbot.domain.request.BitunixOperateAllRequest;
import io.sauce.tradingbot.domain.response.*;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static io.sauce.tradingbot.constants.BitunixApiUrl.*;

@Slf4j
@Component
public class BitunixApiClient {

    public static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    /**
     * place Order
     *
     * @param request
     * @param profileInfo
     * @return
     */
    public PlaceOrderResp placeOrder(BitunixPlaceOrderRequest request, ProfileInfo profileInfo) {
        TreeMap<String, Object> stringStringTreeMap = objectToTreeMap(request);
        String body = doPost(PLACE_ORDER, stringStringTreeMap, profileInfo);
        JavaType javaType = JSONUtil.getObjectMapper().getTypeFactory().constructParametricType(BitunixResponse.class, PlaceOrderResp.class);
        BitunixResponse<PlaceOrderResp> bitunixResponse = JSONUtil.readToEntity(body, javaType);
        return parseResult(request, bitunixResponse, profileInfo, GET_ORDERS);
    }

    public CancelAllOrderResp cancelAllOrders(BitunixOperateAllRequest request, ProfileInfo profileInfo) {
        TreeMap<String, Object> stringStringTreeMap = objectToTreeMap(request);
        String body = doPost(CANCEL_ORDER, stringStringTreeMap, profileInfo);
        JavaType javaType = JSONUtil.getObjectMapper().getTypeFactory().constructParametricType(BitunixResponse.class, CancelAllOrderResp.class);
        BitunixResponse<CancelAllOrderResp> bitunixResponse = JSONUtil.readToEntity(body, javaType);
        return parseResult(request, bitunixResponse, profileInfo, GET_ORDERS);
    }

    public void closeAllPosition(BitunixOperateAllRequest request, ProfileInfo profileInfo) {
        TreeMap<String, Object> stringStringTreeMap = objectToTreeMap(request);
        doPost(CLOSE_POSITION, stringStringTreeMap, profileInfo);
    }

    public BitunixOrderResp getOrders(String symbol, ProfileInfo profileInfo) {
        TreeMap<String, String> stringStringTreeMap = new TreeMap<>();
        stringStringTreeMap.put("symbol", symbol);
        String body = doGet(GET_ORDERS, stringStringTreeMap, profileInfo);

        TypeFactory typeFactory = JSONUtil.getObjectMapper().getTypeFactory();
        JavaType javaType = typeFactory.constructParametricType(BitunixResponse.class, BitunixOrderResp.class);
        BitunixResponse<BitunixOrderResp> bitunixResponse = JSONUtil.readToEntity(body, javaType);
        return parseResult(symbol, bitunixResponse, profileInfo, GET_ORDERS);
    }

    public List<BitunixPositionResp> getPositions(String symbol, ProfileInfo profileInfo) {
        TreeMap<String, String> stringStringTreeMap = new TreeMap<>();
        stringStringTreeMap.put("symbol", symbol);
        String body = doGet(GET_POSITIONS, stringStringTreeMap, profileInfo);
        TypeFactory typeFactory = JSONUtil.getObjectMapper().getTypeFactory();
        JavaType listType = typeFactory.constructParametricType(List.class, BitunixPositionResp.class);
        JavaType javaType = typeFactory.constructParametricType(BitunixResponse.class, listType);
        BitunixResponse<List<BitunixPositionResp>> bitunixResponse = JSONUtil.readToEntity(body, javaType);
        return parseResult(symbol, bitunixResponse, profileInfo, GET_ORDERS);
    }

    public List<TradingPair> getCoinPairs(Set<String> coinPairs) {
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put("symbols", coinPairs.stream().collect(Collectors.joining(",")));
        String body = doGet(BitunixApiUrl.GET_COIN_PAIR, paramMap, null);
        TypeFactory typeFactory = JSONUtil.getObjectMapper().getTypeFactory();
        JavaType listType = typeFactory.constructParametricType(List.class, TradingPair.class);
        JavaType javaType = typeFactory.constructParametricType(BitunixResponse.class, listType);
        BitunixResponse<List<TradingPair>> bitunixResponse = JSONUtil.readToEntity(body, javaType);
        return parseResult(coinPairs, bitunixResponse, null, GET_ORDERS);
    }

    public BitunixLeverageResp getLeverage(String symbol, ProfileInfo profileInfo) {
        TreeMap<String, String> paramMap = new TreeMap<>();
        paramMap.put("symbol", symbol);
        paramMap.put("marginCoin", "USDT");
        String body = doGet(GET_LEVERAGE_MARGIN_MODE, paramMap, profileInfo);
        TypeFactory typeFactory = JSONUtil.getObjectMapper().getTypeFactory();
        JavaType javaType = typeFactory.constructParametricType(BitunixResponse.class, BitunixLeverageResp.class);
        BitunixResponse<BitunixLeverageResp> bitunixResponse = JSONUtil.readToEntity(body, javaType);
        return parseResult(symbol, bitunixResponse, profileInfo, GET_ORDERS);
    }

    public void modifyLeverage(BitunixChangeLeverageRequest request, ProfileInfo profileInfo) {
        TreeMap<String, Object> stringStringTreeMap = objectToTreeMap(request);
        doPost(SET_LEVERAGE, stringStringTreeMap, profileInfo);
    }


    private static String doGet(String url, TreeMap<String, String> paramMap, ProfileInfo profileInfo) {
        // url
        StringBuilder stringBuilder = new StringBuilder(BASE_URL + url);
        if (paramMap != null && !paramMap.isEmpty()) {
            stringBuilder.append("?");
        }
        for (Map.Entry<String, String> stringStringEntry : paramMap.entrySet()) {
            if (!stringBuilder.toString().endsWith("?")) {
                stringBuilder.append("&");
            }
            stringBuilder.append(stringStringEntry.getKey()).append("=").append(stringStringEntry.getValue());
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(new Random().nextInt(1000000000));
        // header
        Headers.Builder header = new Headers.Builder();
        if (profileInfo != null) {
            header.add("api-key", profileInfo.getApiKey());
            header.add("sign", SignatureGenerator.generateSign(nonce, timestamp, profileInfo.getApiKey(), paramMap, "", profileInfo.getApiSecret()));
        }
        header.add("timestamp", timestamp);
        header.add("nonce", nonce);
        header.add("content-type", "application/json");
        Headers build = header.build();
        Request request = new Request.Builder().url(stringBuilder.toString()).headers(build).get().build();
        try {
            return doRequest(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String doPost(String url, Map<String, Object> bodyMap, ProfileInfo profileInfo) {
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), JSONUtil.toJsonString(bodyMap));

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = String.valueOf(new Random().nextInt(1000000000));
        // header
        Headers.Builder header = new Headers.Builder();
        header.add("api-key", profileInfo.getApiKey());
        header.add("timestamp", timestamp);
        header.add("nonce", nonce);
        header.add("sign", SignatureGenerator.generateSign(nonce, timestamp, profileInfo.getApiKey(), null, JSONUtil.toJsonString(bodyMap), profileInfo.getApiSecret()));

        Request request = new Request.Builder().url(BASE_URL + url).headers(header.build()).post(requestBody).build();
        try {
            log.info("[http] request bitunix, url:{} params:{}", url, JSONUtil.toJsonString(bodyMap));
            String bodyString = doRequest(request);
            log.info("[http] response from bitunix, url:{}, data:{}", request.url(), bodyString);
            return bodyString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static String doRequest(Request request) throws IOException {
        Response response = OK_HTTP_CLIENT.newCall(request).execute();
        ResponseBody body = response.body();
        if (body == null) {
            throw new RuntimeException("request failed");
        }
        String bodyString = body.string();
        if (response.code() != 200) {
            throw new RuntimeException("request failed");
        }
        return bodyString;
    }

    private TreeMap<String, Object> objectToTreeMap(Object object) {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);
                if (value != null) {
                    treeMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.error("Failed to get field value: {}", field.getName(), e);
            }
        }
        return treeMap;
    }

    private <R> R parseResult(Object request, BitunixResponse<R> bitunixResponse, ProfileInfo profileInfo, String url) {
        Integer code = bitunixResponse.getCode();
        if (code != 0) {
            String apiKey = profileInfo == null ? null : profileInfo.getApiKey();
            log.error("request bitunix error, api_key:{}, url:{}, request:{}, code:{}, msg:{}", apiKey, url, JSONUtil.toJsonString(request), code, bitunixResponse.getMsg());
            return null;
        }
        return bitunixResponse.getData();
    }


    @Data
    private static class BitunixResponse<T> {
        private Integer code;
        private T data;
        private String msg;
    }
}
