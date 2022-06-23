package com.learning.core.exception;

import com.learning.core.enums.ApiCode;

public class ElasticException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private Integer code;

    public ElasticException() {
    }

    public ElasticException(String msg) {
        super(msg);
        this.code = 500;
    }

    public ElasticException(Exception e) {
        super(e.getMessage());
        this.code = 500;
    }

    public ElasticException(ApiCode apiCode) {
        super(apiCode.getMessage());
        this.code = apiCode.getCode();
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
