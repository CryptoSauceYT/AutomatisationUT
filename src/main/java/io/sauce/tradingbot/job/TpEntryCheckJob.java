package io.sauce.tradingbot.job;

import io.sauce.tradingbot.compoent.handler.TpEntryCheckHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TpEntryCheckJob {

    @Resource
    private TpEntryCheckHandler tpEntryCheckHandler;

    @Scheduled(fixedRate = 3000)
    public void execute() {
        tpEntryCheckHandler.checkTpEntry();
    }

}
