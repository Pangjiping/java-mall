package org.epha.mall.order.configuration;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author pangjiping
 * springsession原理：https://www.bilibili.com/video/BV1np4y1C7Yf/?p=229&spm_id_from=pageDriver&vd_source=958b6f7b1b41557b106fd2d06e131786
 */
@Configuration
public class SessionConfiguration {

    /**
     * 修改session的作用域
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        // 设置session作用域为整个父域
        cookieSerializer.setDomainName("localhost");

        // 设置session的名字
        cookieSerializer.setCookieName("JMALLSESSION");

        return cookieSerializer;
    }

    /**
     * 修改session默认的序列化机制为json
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericFastJsonRedisSerializer();
    }
}
