package org.epha.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.constant.OrderConstant;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.common.utils.R;
import org.epha.mall.seckill.feign.CouponFeignService;
import org.epha.mall.seckill.interceptor.LoginUserInterceptor;
import org.epha.mall.seckill.service.SeckillService;
import org.epha.mall.seckill.vo.LoginUser;
import org.epha.mall.seckill.vo.SeckillOrder;
import org.epha.mall.seckill.vo.SeckillSession;
import org.epha.mall.seckill.vo.SeckillSkuRelation;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author pangjiping
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private static final String SKUKILL_CACHE_HASH_KEY = "seckill:skus";

    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    private final ThreadLocal<SeckillSkuRelation> seckillSkuRelation = new ThreadLocal<>();

    @Resource
    CouponFeignService couponFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Override
    public void upSeckillSku() {
        // 扫描需要秒杀的商品
        R r = couponFeignService.listThreeDaysSession();
        if (r.getCode() != 0) {
            // TODO 远程调用出错
            return;
        }

        List<SeckillSession> sessions = r.getData(new TypeReference<>() {
        });

        // 缓存到redis里面
        // 缓存活动信息
        cacheSessionInfo(sessions);

        // 缓存活动的商品信息
        cacheSessionSkuInfo(sessions);
    }

    @Override
    public List<SeckillSkuRelation> listCurrentSkus() {

        // 确定当前时间属于哪个秒杀场次
        long time = System.currentTimeMillis();

        // 拿到所有的key然后遍历
        Set<String> keys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        if (keys == null || keys.size() <= 0) {
            return new ArrayList<>();
        }

        for (String key : keys) {
            String[] times = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
            long startTime = Long.parseLong(times[0]);
            long endTime = Long.parseLong(times[1]);
            if (time >= startTime && time <= endTime) {
                // 获取场次的商品信息
                List<String> stringList = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_HASH_KEY);
                List<String> objects = hashOps.multiGet(stringList);
                if (objects != null && objects.size() > 0) {
                    return objects.stream()
                            .map(obj -> {
                                SeckillSkuRelation relation = JSON.parseObject(obj, new TypeReference<SeckillSkuRelation>() {
                                });
                                relation.setRandomCode(null);
                                return relation;
                            })
                            .collect(Collectors.toList());
                }
                break;
            }
        }

        return new ArrayList<>();
    }

    @Override
    public String kill(String killId, String randomCode, Integer number) throws BizException {
        // 合法性校验
        validateRandomCode(killId, randomCode, number);

        // 获取信号量
        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
        try {
            boolean acquire = semaphore.tryAcquire(number, 100, TimeUnit.MILLISECONDS);
            if (acquire) {
                // 秒杀成功
                // 给MQ发送一个消息
                String orderSn = IdWorker.getTimeId();
                sendOrderMessage(orderSn, number);
                return orderSn;
            } else {
                throw new BizException(BizCodeEnum.FAIL_SECKILL);
            }
        } catch (InterruptedException e) {
            log.error("获取信号量失败：{}", e.getMessage());
            throw new BizException(BizCodeEnum.FAIL_SECKILL);
        }

    }

    private void sendOrderMessage(String orderSn, Integer number) {

        LoginUser user = LoginUserInterceptor.threadLocal.get();
        SeckillSkuRelation skuRelation = this.seckillSkuRelation.get();

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderSn(orderSn);
        seckillOrder.setMemberId(user.getId());
        seckillOrder.setNumber(new BigDecimal(number));
        seckillOrder.setPromotionSessionId(skuRelation.getPromotionSessionId());
        seckillOrder.setSkuId(skuRelation.getSkuId());
        seckillOrder.setSeckillPrice(skuRelation.getSeckillPrice());

        String uuid = UUID.randomUUID().toString().replace("-", "");
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(uuid);
        properties.setContentType("text/plain");
        properties.setContentEncoding("utf-8");

        String content = JSON.toJSONString(seckillOrder);
        Message message = new Message(content.getBytes(StandardCharsets.UTF_8), properties);
        CorrelationData correlationData = new CorrelationData(uuid);

        try {
            rabbitTemplate.convertAndSend(
                    OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                    "order.seckill.order",
                    message,
                    correlationData);
        } catch (Exception e) {
            log.error("秒杀订单消息发送失败: {}", e.getMessage());
        }
    }

    private void validateRandomCode(String killId, String randomCode, Integer number) throws BizException {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_HASH_KEY);
        String s = hashOps.get(killId);
        if (!StringUtils.hasText(s)) {
            throw new BizException(BizCodeEnum.INVALID_SKUKILL_ID);
        }

        SeckillSkuRelation skuRelation = JSON.parseObject(s, new TypeReference<>() {
        });
        // 判断秒杀时间
        Long startTime = skuRelation.getStartTime();
        Long endTime = skuRelation.getEndTime();
        if (System.currentTimeMillis() < startTime) {
            throw new BizException(BizCodeEnum.SECKILL_UNSTART);
        } else if (System.currentTimeMillis() > endTime) {
            throw new BizException(BizCodeEnum.SECKILL_TIMEOUT);
        }

        // 判断随机code
        String code = skuRelation.getRandomCode();
        String skuId = skuRelation.getPromotionSessionId() + "_" + skuRelation.getSkuId();
        if (!randomCode.equals(code) || !killId.equals(skuId)) {
            throw new BizException(BizCodeEnum.INVALID_RANDOM_CODE);
        }

        // 判断
        if (number > skuRelation.getSeckillLimit().intValue()) {
            throw new BizException(BizCodeEnum.INVALID_COUNT);
        }

        // 幂等性校验
        LoginUser user = LoginUserInterceptor.threadLocal.get();

        String redisKey = user.getId().toString() + "_" + skuId;
        Boolean ifAbsent = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, number.toString(), endTime - startTime, TimeUnit.MILLISECONDS);
        if (ifAbsent == null || !ifAbsent) {
            throw new BizException(BizCodeEnum.INVALID_USER);
        }

        seckillSkuRelation.set(skuRelation);
    }

    private void cacheSessionInfo(List<SeckillSession> sessions) {
        sessions.forEach(seckillSession -> {
            long startTime = seckillSession.getStartTime().getTime();
            long endTime = seckillSession.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;


            if (!stringRedisTemplate.hasKey(key)) {
                List<String> skuIds = seckillSession.getRelationEntities().stream()
                        .map(item -> item.getPromotionSessionId().toString() + "-" + item.getSkuId().toString())
                        .collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, skuIds);
            }
        });
    }

    private void cacheSessionSkuInfo(List<SeckillSession> sessions) {

        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_HASH_KEY);

        sessions.forEach(seckillSession ->
                seckillSession.getRelationEntities().forEach(seckillSkuRelation -> {

                    // 查sku详细信息，没写

                    // 设置秒杀随机码
                    String token = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRelation.setRandomCode(token);

                    if (!hashOps.hasKey(seckillSkuRelation.getPromotionSessionId().toString() + "_"
                            + seckillSkuRelation.getSkuId().toString())) {
                        String jsonString = JSON.toJSONString(seckillSkuRelation);
                        hashOps.put(seckillSkuRelation.getPromotionSessionId().toString() + "_" + seckillSkuRelation.getSkuId().toString(), jsonString);
                    }

                    // 引入分布式信号量 - 限流
                    if (!stringRedisTemplate.hasKey(SKU_STOCK_SEMAPHORE + token)) {
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                        semaphore.trySetPermits(seckillSkuRelation.getSeckillCount().intValue());
                    }

                })
        );
    }
}
