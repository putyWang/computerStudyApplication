package com.learning.shiro.utils;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "f4e2e52034348f86b67cde581c0f9eb5";
    /**
     * 过期时间 默认一周
     */
    private long expire = 604800L;
    private String header;
}
