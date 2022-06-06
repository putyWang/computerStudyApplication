package com.learning.exception.exception;

import org.apache.shiro.authc.AccountException;

public class verificationCodeErrorException
        extends AccountException {
    public verificationCodeErrorException() {
    }

    public verificationCodeErrorException(String message) {
        super(message);
    }

    public verificationCodeErrorException(Throwable cause) {
        super(cause);
    }

    public verificationCodeErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
