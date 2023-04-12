package org.epha.mall.auth.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author pangjiping
 */
@Configuration
public class SpringMvcConfiguration implements WebMvcConfigurer {

    /**
     * 视图映射，不需要写跳转逻辑相关的controller了
     */
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("login");
//        registry.addViewController("/reg.html").setViewName("reg");
//    }
}
