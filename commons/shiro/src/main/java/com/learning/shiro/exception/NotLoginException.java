package com.learning.shiro.exception;

import com.learning.core.exception.SpringBootException;

import java.util.Arrays;
import java.util.List;

/**
 * 用户未登录异常
 */
public class NotLoginException extends SpringBootException {

    private static final long serialVersionUID = 6806129545290130142L;
    public static final String NOT_TOKEN = "-1";
    public static final String NOT_TOKEN_MESSAGE = "未提供Token";
    public static final String INVALID_TOKEN = "-2";
    public static final String INVALID_TOKEN_MESSAGE = "Token无效";
    public static final String TOKEN_TIMEOUT = "-3";
    public static final String TOKEN_TIMEOUT_MESSAGE = "Token已过期";
    public static final String BE_REPLACED = "-4";
    public static final String BE_REPLACED_MESSAGE = "Token已被顶下线";
    public static final String KICK_OUT = "-5";
    public static final String KICK_OUT_MESSAGE = "Token已被踢下线";
    public static final String DEFAULT_MESSAGE = "当前会话未登录";
    public static final List<String> ABNORMAL_LIST = Arrays.asList("-1", "-2", "-3", "-4", "-5");
    private String type;
    private String loginType;

    public String getType() {
        return this.type;
    }

    public String getLoginType() {
        return this.loginType;
    }

    public NotLoginException(String message, String loginType, String type) {
        super(message);
        this.loginType = loginType;
        this.type = type;
    }

    public static NotLoginException newInstance(String loginType, String type) {
        return newInstance(loginType, type, null);
    }

    public static NotLoginException newInstance(String loginType, String type, String token) {
        String message = null;
        if ("-1".equals(type)) {
            message = "未提供Token";
        } else if ("-2".equals(type)) {
            message = "Token无效";
        } else if ("-3".equals(type)) {
            message = "Token已过期";
        } else if ("-4".equals(type)) {
            message = "Token已被顶下线";
        } else if ("-5".equals(type)) {
            message = "Token已被踢下线";
        } else {
            message = "当前会话未登录";
        }

        return new NotLoginException(message, loginType, type);
    }
}
