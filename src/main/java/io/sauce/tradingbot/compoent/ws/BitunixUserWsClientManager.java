package io.sauce.tradingbot.compoent.ws;

import io.sauce.tradingbot.domain.ProfileInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BitunixUserWsClientManager {

    /**
     *  key: profileName
     *  value: BitunixUserWsClient
     */
    private final Map<String, BitunixUserWsClient> bitunixUserWsClientMap = new ConcurrentHashMap<>();

    public BitunixUserWsClient getClient(String profileName) {
        return bitunixUserWsClientMap.get(profileName);
    }

    public void addClient(ProfileInfo profileInfo) {

    }

    public void removeClient(String profileName) {

    }

    public boolean existClient(String profileName) {
        return bitunixUserWsClientMap.containsKey(profileName);
    }


}
