package com.learning.core.constants;

public class RedisConstants {

    /**
     * 密码输错次数key
     */
    public static final String ERROR_NUMBER_OF_PASSWORD = "errorNumberOfPassword";

    /**
     * 用户锁定后主键
     */
    public static final String USER_LOCKED_KEY = "LOCKED";

    /**
     * 输错多次密码后锁定时间
     */
    public static final Long DEFAULT_USER_LOCKED_TIME_OUT = 600L;


    public static final Long DEFAULT_VERIFICATION_CODE_TIME_OUT = 120L;
}
