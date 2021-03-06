package com.learning.exception.exception;

import com.learning.core.enums.ApiCode;

/**
 * 异常构造类
 */
public class ExceptionBuilder {

    private ExceptionBuilder() {}

    public static BaseException build(String message) {
        return new BaseException(ApiCode.SYSTEM_EXCEPTION.getCode(), message);
    }

    public static BaseException build(Integer errorCode, String message) {
        return new BaseException(errorCode, message);
    }

    public static BaseException build(Integer errorCode, String message, Object... args) {
        return new BaseException(errorCode, message, args);
    }
}
