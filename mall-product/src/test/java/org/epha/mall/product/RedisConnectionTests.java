package org.epha.mall.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.UUID;

@SpringBootTest
public class RedisConnectionTests {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void basicConnectionTest(){
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();

        // 保存
        opsForValue.set("Hello","World_"+ UUID.randomUUID().toString());

        // 查询
        String s = opsForValue.get("Hello");
        System.out.println(s);
    }
}
