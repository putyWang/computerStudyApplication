package com.learning.shiro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shiro")
public class ShiroProperties {

    /**
     * 权限拦截路径
     */
    private String authorizedUrl;

    /**
     * 权限拦截忽略的路径
     */
    private String anonUrl;
}
