package io.sauce.tradingbot.config;

import io.sauce.tradingbot.compoent.handler.DepthHandler;
import io.sauce.tradingbot.compoent.handler.TpEntryCheckHandler;
import io.sauce.tradingbot.compoent.ws.BitunixPublicWsClient;
import io.sauce.tradingbot.compoent.provider.PriceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class WebSocketClientConfig {

    @Resource
    private BotConfigProperties botConfigProperties;
    @Resource
    private PriceProvider priceProvider;
    @Resource
    private TpEntryCheckHandler tpEntryCheckHandler;
    @Resource
    private DepthHandler depthHandler;


    @Bean
    public BitunixPublicWsClient myWebSocketClientHandler() throws URISyntaxException {
        URI uri = new URI("wss://fapi.bitunix.com/public/");
        BitunixPublicWsClient client = new BitunixPublicWsClient(uri,
                botConfigProperties,
                priceProvider,
                depthHandler,
                tpEntryCheckHandler);
        client.connect();
        return client;
    }
}