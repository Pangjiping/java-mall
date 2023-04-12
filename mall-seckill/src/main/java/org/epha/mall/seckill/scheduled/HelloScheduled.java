package org.epha.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author pangjiping
 */
//@EnableScheduling
//@EnableAsync
@Component
@Slf4j
public class HelloScheduled {

    /**
     * 开启一个定时任务
     * spring中只允许6位，没有年这个单位
     * 默认定时任务是阻塞的
     */
    @Async
    @Scheduled(cron = "* * * * * ?")
    public void hello(){
        log.info("Hello...");
    }
}
