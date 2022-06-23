package com.learning.es.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * canal配置类
 */
@Component
@ConfigurationProperties("canal")
public class CanalConfigurationProperties {

    private String hostname = "127.0.0.1";

    private Integer port = 11111;

    /**
     * 设置订阅
     */
    private String destination = "example";

    private String username = "";

    private String password = "";

    /**
     * 是否允许重复链接
     */
    private boolean isRepeatConnect = false;

    /**
     * 设置接收对象路径
     */
    @Value("${canal.subscribe.regex}")
    private String subscribeRegex = "boot-admin\\..*";

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRepeatConnect() {
        return isRepeatConnect;
    }

    public void setRepeatConnect(boolean repeatConnect) {
        isRepeatConnect = repeatConnect;
    }

    public String getSubscribeRegex() {
        return subscribeRegex;
    }

    public void setSubscribeRegex(String subscribeRegex) {
        this.subscribeRegex = subscribeRegex;
    }
}
