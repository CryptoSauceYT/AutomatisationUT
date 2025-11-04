package io.sauce.tradingbot.compoent.ws;

import io.sauce.tradingbot.compoent.handler.DepthHandler;
import io.sauce.tradingbot.compoent.handler.TpEntryCheckHandler;
import io.sauce.tradingbot.config.BotConfigProperties;
import io.sauce.tradingbot.domain.LastPriceItem;
import io.sauce.tradingbot.domain.request.BitunixWsRequest;
import io.sauce.tradingbot.domain.request.DepthSubRequest;
import io.sauce.tradingbot.domain.request.PingRequest;
import io.sauce.tradingbot.domain.response.BitunixWsResp;
import io.sauce.tradingbot.domain.response.DepthWsResp;
import io.sauce.tradingbot.compoent.provider.PriceProvider;
import io.sauce.tradingbot.util.JSONUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class BitunixPublicWsClient extends WebSocketClient {

    private static final int HEARTBEAT_INTERVAL = 3000;
    private static final long RETRY_INTERVAL = 3000;
    private static final String DEPTH_BOOK_1 = "depth_book1";
    private Timer heartbeatTimer;
    private Timer retryTimer;

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final BotConfigProperties botConfigProperties;
    private final PriceProvider priceProvider;
    private DepthHandler depthHandler;
    private TpEntryCheckHandler tpEntryCheckHandler;

    public BitunixPublicWsClient(URI serverUri,
                                 BotConfigProperties botConfigProperties,
                                 PriceProvider priceProvider,
                                 DepthHandler depthHandler,
                                 TpEntryCheckHandler tpEntryCheckHandler) {
        super(serverUri);
        this.botConfigProperties = botConfigProperties;
        this.priceProvider = priceProvider;
        this.depthHandler = depthHandler;
        this.tpEntryCheckHandler = tpEntryCheckHandler;
        initializeRetryTimer();
        initializeHeartbeat();
    }


    private void initializeRetryTimer() {
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer.purge();
        }
        retryTimer = new Timer("Bitunix-Retry-Timer");
    }


    private void startRetryTask() {
        // 清理之前的任务
        initializeRetryTimer();
        retryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isConnected.get()) {
                    log.warn("try reconnect bitunix public ws...");
                    reconnect();
                }
            }
        }, RETRY_INTERVAL, RETRY_INTERVAL);
    }

    /**
     * 停止重试任务。
     */
    private void stopRetryTask() {
        if (retryTimer != null) {
            retryTimer.cancel();
            retryTimer.purge();
            retryTimer = null;
        }
    }

    @SneakyThrows
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("[bitunix public ws] connected !!");
        isConnected.set(true);
        stopRetryTask();
        Set<String> coinPairs = botConfigProperties.getTradingPairs();
        // subscribe all CoinPair channel
        List<DepthSubRequest> depthSubRequests = coinPairs.stream()
                .map(coinPair -> new DepthSubRequest(coinPair, DEPTH_BOOK_1))
                .collect(Collectors.toList());
        BitunixWsRequest bitunixWsRequest = new BitunixWsRequest();
        bitunixWsRequest.setOp("subscribe");
        bitunixWsRequest.setArgs(depthSubRequests);
        this.send(JSONUtil.toJsonString(bitunixWsRequest));
    }

    @Override
    public void onMessage(String message) {
        BitunixWsResp bitunixWsResp = JSONUtil.readToEntity(message, BitunixWsResp.class);
        if (bitunixWsResp == null) {
            log.info("[bitunix public ws] unexpect ws data, ignore. data:{}", message);
            return;
        }
        if ("ping".equals(bitunixWsResp.getOp()) || "connect".equals(bitunixWsResp.getOp())) {
            return;
        }
        if (bitunixWsResp.getCh() == null || !bitunixWsResp.getCh().equals(DEPTH_BOOK_1)) {
            log.info("[bitunix public ws] unexpect ws data, ignore. data:{}", message);
            return;
        }

        long timeDiff = Instant.now().toEpochMilli() - Long.parseLong(bitunixWsResp.getTs());
        if (timeDiff > 5000) {
            log.warn("[bitunix public ws] ws data is too old( diff {} ms), ignore. data:{}", timeDiff, message);
            return;
        }
        depthHandler.handle(bitunixWsResp);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("[bitunix public ws] closed, code:{}, reason:{}, remote:{}", code, reason, remote);
        isConnected.set(false);
        startRetryTask();
    }

    @Override
    public void onError(Exception ex) {
        log.error("[bitunix public ws] error:{}", ex.getMessage());
        isConnected.set(false);
        startRetryTask();
    }

    private void initializeHeartbeat() {
        heartbeatTimer = new Timer("Bitunix-Heartbeat-Timer", true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 只有在连接状态时才发送ping
                if (isConnected.get()) {
                    PingRequest pingRequest = new PingRequest();
                    pingRequest.setOp("ping");
                    pingRequest.setPing(Instant.now().getEpochSecond());
                    String msg = JSONUtil.toJsonString(pingRequest);
                    send(msg);
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
    }
}