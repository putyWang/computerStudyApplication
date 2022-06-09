package com.learning.core.config;

import com.learning.core.xss.XssFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;

@Configuration
public class FilterConfig {

    /**
     * 注册xxs过滤器
     * @return
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        //设置过滤器调用类型
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setFilter(new XssFilter());
        //过滤所有路径
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        //设置注册顺序
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }
}
