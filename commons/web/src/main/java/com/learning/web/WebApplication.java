package com.learning.web;

import com.learning.core.config.DruidProperties;
import com.learning.shiro.config.ShiroProperties;
import com.learning.swagger.config.SwaggerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
        DruidProperties.class,
        ShiroProperties.class,
        SwaggerProperties.class
})
@SpringBootApplication(scanBasePackages = {
        "com.learning"
})
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
