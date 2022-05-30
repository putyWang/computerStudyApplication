package com.learning.core.exception;

import com.learning.core.enums.ApiCode;

public class VerificationCodeException extends SpringBootException {
    private static final long serialVersionUID = -2640690119865434398L;

    public VerificationCodeException(String message) {
        super(message);
    }

    public VerificationCodeException(Integer errorCode, String message) {
        super(errorCode, message);
    }

    public VerificationCodeException(ApiCode apiCode) {
        super(apiCode);
    }
}
