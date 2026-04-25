package com.auth.exception;

public class InvalidTotpException extends RuntimeException {
    public InvalidTotpException(String message) {
        super(message);
    }
}