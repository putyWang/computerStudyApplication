package com.learning.core.exception;

import com.learning.core.enums.ApiCode;

public class DaoException extends SpringBootException {
    private static final long serialVersionUID = -6912618737345878854L;

    public DaoException(String message) {
        super(message);
    }

    public DaoException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public DaoException(ApiCode apiCode) {
        super(apiCode);
    }
}