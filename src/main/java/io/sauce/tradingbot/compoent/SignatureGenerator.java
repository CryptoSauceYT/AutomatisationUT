package io.sauce.tradingbot.compoent;


import io.micrometer.core.instrument.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SignatureGenerator {

    public static String generateSign(String nonce,
                                      String timestamp,
                                      String apiKey,
                                      TreeMap<String, String> queryParamsMap,
                                      String httpBody,
                                      String secretKey) {

        StringBuilder queryString = null;
        if (queryParamsMap != null && !queryParamsMap.isEmpty()) {
            queryString = new StringBuilder();
            Set<Map.Entry<String, String>> entrySet = queryParamsMap.entrySet();
            for (Map.Entry<String, String> param : entrySet) {
                if (param.getKey().equals("sign")) {
                    continue;
                }
                if (!StringUtils.isBlank(param.getValue())) {
                    queryString.append(param.getKey());
                    queryString.append(param.getValue());
                }
            }
        }
        String baseSignStr = nonce + timestamp + apiKey;
        if (queryString != null) {
            baseSignStr += queryString.toString();
        }
        String digest = SHAUtils.encrypt(baseSignStr, httpBody);
        return SHAUtils.encrypt(digest + secretKey);
    }
}
