package com.learning.exception.exception;

import com.learning.core.enums.ApiCode;

public class ElasticException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private Integer code;

    public ElasticException() {
        super(ApiCode.ELASTICSEARCH_REQUEST_EXCEPTION.getMessage());
        this.code = ApiCode.ELASTICSEARCH_REQUEST_EXCEPTION.getCode();
    }

    public ElasticException(String msg) {
        super(msg);
        this.code = ApiCode.ELASTICSEARCH_REQUEST_EXCEPTION.getCode();
    }

    public ElasticException(Exception e) {
        super(e.getMessage());
        this.code = ApiCode.ELASTICSEARCH_REQUEST_EXCEPTION.getCode();
    }

    public ElasticException(ApiCode code) {
        super(code.getMessage());
        this.code = code.getCode();
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
