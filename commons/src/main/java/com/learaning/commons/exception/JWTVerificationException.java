package com.learaning.commons.exception;

public class JWTVerificationException extends RuntimeException {
    public JWTVerificationException(String message) {
        this(message, (Throwable)null);
    }

    public JWTVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
