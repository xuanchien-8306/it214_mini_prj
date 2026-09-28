package com.rikkeibank.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int status;
    private final String error;

    public BusinessException(String message, int status, String error) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public BusinessException(String message) {
        this(message, 400, "Bad Request");
    }
}
