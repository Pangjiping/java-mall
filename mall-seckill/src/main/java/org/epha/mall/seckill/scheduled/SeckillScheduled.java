package org.epha.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.epha.mall.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 每天晚上3点，上架最新三天需要秒杀的商品
 *
 * @author pangjiping
 */
@Slf4j
@Service
public class SeckillScheduled {

    public static final String UPLOAD_LOCK = "seckill:upload:lock";

    @Resource
    SeckillService seckillService;

    @Resource
    RedissonClient redissonClient;

    @Scheduled(cron = "0 0 3 * * ?")
    public void upSeckillSkuForThreeDays() {
        log.info("商品上架开始...");

        // 加入分布式锁，防止重复上架
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);

        try {
            seckillService.upSeckillSku();
        } finally {
            lock.unlock();
        }

    }
}
