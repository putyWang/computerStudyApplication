package com.learning.core.exception;

public class JWTDecodeException extends JWTVerificationException {
    public JWTDecodeException(String message) {
        this(message, (Throwable)null);
    }

    public JWTDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}