package io.sauce.tradingbot.compoent.ws;

import io.sauce.tradingbot.compoent.handler.TpEntryCheckHandler;
import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.domain.ProfileInfo;
import io.sauce.tradingbot.domain.request.PingRequest;
import io.sauce.tradingbot.compoent.provider.PriceProvider;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class BitunixUserWsClient extends WebSocketClient {

    private static final int HEARTBEAT_INTERVAL = 20000;
    private static final long RETRY_INTERVAL = 5000;
    private Timer heartbeatTimer;
    private Timer retryTimer;

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final BotConfigProperties botConfigProperties;
    private final PriceProvider priceProvider;
    private final ProfileInfo profileInfo;
    private final String profileName;

    public BitunixUserWsClient(URI serverUri,
                               BotConfigProperties botConfigProperties,
                               PriceProvider priceProvider,
                               ProfileInfo profileInfo,
                               String profileName) {
        super(serverUri);
        initializeRetryTimer();
        this.botConfigProperties = botConfigProperties;
        this.priceProvider = priceProvider;
        this.profileInfo = profileInfo;
        this.profileName = profileName;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        
    }

    @Override
    public void onMessage(String s) {
        log.info("bitunix private [{}] ws connected !!", profileName);
        isConnected.set(true);
        stopRetryTask();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("bitunix private [{}] ws closed, code:{}, reason:{}, remote:{}", profileInfo, code, reason, remote);
        isConnected.set(false);
        startRetryTask();
    }

    @Override
    public void onError(Exception ex) {
        log.error("bitunix private [{}] ws error:{}", profileInfo, ex.getMessage());
        isConnected.set(false);
        startRetryTask();
    }


    private void initializeRetryTimer() {
        retryTimer = new Timer("Bitunix-Retry-Timer" + profileName);
    }

    private void initializeHeartbeat() {
        heartbeatTimer = new Timer("Bitunix-HeartBeat-Timer" + profileName, true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                while (!isConnected.get()) {
                    try {
                        log.info("Waiting for bitunix private [{}] ws connected...", profileInfo);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("heartbeat error:{}", e.getMessage());
                    }
                }
                PingRequest pingRequest = new PingRequest();
                pingRequest.setOp("ping");
                pingRequest.setPing(Instant.now().getEpochSecond());
                String msg = JSONUtil.toJsonString(pingRequest);
                send(msg);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
    }


    private void startRetryTask() {
        retryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isConnected.get()) {
                    log.warn("try reconnect bitunix ...");
                    reconnect();
                }
            }
        }, RETRY_INTERVAL, RETRY_INTERVAL);
    }

    /**
     * 停止重试任务。
     */
    private void stopRetryTask() {
        retryTimer.cancel();
        retryTimer.purge();
        initializeRetryTimer();
        initializeHeartbeat();
    }

}
