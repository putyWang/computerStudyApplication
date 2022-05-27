package com.learning.commons.config;

import com.learning.commons.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.HashMap;
import java.util.Map;

@Configurable
public class springConfig {

    /**
     * 注册JwtAuthFilter
     * @return
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> securityFilterRegistration() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        DelegatingFilterProxy httpBasicFilter = new DelegatingFilterProxy();
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter();
        registration.setFilter(jwtAuthFilter);
        Map<String, String> m = new HashMap<>();
        m.put("targetBeanName", "jwtAuthFilter");
        m.put("targetFilterLifecycle", "true");
        registration.setInitParameters(m);

        registration.setOrder(2);
        return registration;
    }

}
