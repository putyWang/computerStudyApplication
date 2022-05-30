package com.learning.core.exception;

import com.learning.core.enums.ApiCode;
import org.apache.shiro.authc.AuthenticationException;

public class UnauthorizedException extends AuthenticationException {
    private static final long serialVersionUID = 1L;
    private Integer code;

    public UnauthorizedException() {
        super(ApiCode.UNAUTHORIZED.getMessage());
        this.code = ApiCode.UNAUTHORIZED.getCode();
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}