package org.epha.mall.product.configuration;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableConfigurationProperties(CacheProperties.class)
@Configuration
@EnableCaching
public class CacheRedisConfiguration {

    /**
     * 自定义RedisCacheConfiguration配置
     *
     * @return
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig();

        // 自定义配置
        // 配置key的序列化方式为string（默认就是string）
        cacheConfig = cacheConfig.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

        // 配置value的序列化方式为json
        cacheConfig = cacheConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));

        // 让配置文件中的所有配置生效
        CacheProperties.Redis cachePropertiesRedis = cacheProperties.getRedis();
        if (cachePropertiesRedis.getTimeToLive() != null) {
            cacheConfig = cacheConfig.entryTtl(cachePropertiesRedis.getTimeToLive());
        }
        if (cachePropertiesRedis.getKeyPrefix() != null) {
            cacheConfig = cacheConfig.prefixKeysWith(cachePropertiesRedis.getKeyPrefix());
        }
        if (!cachePropertiesRedis.isCacheNullValues()) {
            cacheConfig = cacheConfig.disableCachingNullValues();
        }
        if (!cachePropertiesRedis.isUseKeyPrefix()) {
            cacheConfig = cacheConfig.disableKeyPrefix();
        }

        return cacheConfig;
    }
}
