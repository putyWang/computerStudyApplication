package com.learning.core.exception;

import com.learning.core.enums.ApiCode;

/**
 * 自定义异常类
 */
public class BaseException
        extends SpringBootException{


    public BaseException() {
        super (ApiCode.SYSTEM_EXCEPTION.getCode(), ApiCode.SYSTEM_EXCEPTION.getMessage());
    }

    public BaseException(String message) {
        super (ApiCode.SYSTEM_EXCEPTION.getCode(), message);
    }

    public BaseException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public BaseException(Integer errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public BaseException(ApiCode apiCode) {
        super(apiCode);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(ApiCode apiCode, Throwable cause) {
        super(apiCode, cause);
    }
}
