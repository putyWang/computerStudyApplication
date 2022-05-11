package com.learaning.commons.exception;

import com.learaning.commons.enums.ApiCode;

public class UnauthorizedException extends RuntimeException {
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