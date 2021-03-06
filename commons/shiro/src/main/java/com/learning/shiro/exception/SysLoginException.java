package com.learning.shiro.exception;

import com.learning.core.enums.ApiCode;
import com.learning.core.exception.SpringBootException;

public class SysLoginException extends SpringBootException {
    private static final long serialVersionUID = -3157438982569715170L;

    public SysLoginException(String message) {
        super(message);
    }

    public SysLoginException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public SysLoginException(ApiCode apiCode) {
        super(apiCode);
    }
}
