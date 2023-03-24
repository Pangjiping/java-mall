package org.epha.mall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 远程调用流程
 * 1. 引入open-feign依赖
 * 2. 编写一个接口，高速SpringBoot这个接口需要调用远程服务
 */
@EnableFeignClients(basePackages = "org.epha.mall.member.feign")
@EnableDiscoveryClient
@MapperScan("org.epha.mall.member.dao")
@SpringBootApplication
public class MallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallMemberApplication.class, args);
    }

}
