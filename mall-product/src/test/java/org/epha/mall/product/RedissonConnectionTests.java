package org.epha.mall.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@SpringBootTest
public class RedissonConnectionTests {

    @Resource
    RedissonClient redissonClient;

    @Test
    public void basicConnectionTest() {
        System.out.println(redissonClient);
    }

    @Test
    public void reentrantLockTest() {

        // 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("anyLock");

        // 加锁
        lock.lock(); // 阻塞式等待
        try {
            log.info("加锁成功，执行业务...");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void readWriteLockTest() {

        // 获取一把读写锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("anyLock");
        
        RLock writeLock = lock.writeLock();
        String s = "";
        try {
            writeLock.lock();

            // 模拟一个业务写场景
            s = UUID.randomUUID().toString();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }

        log.info(s);
    }
}
