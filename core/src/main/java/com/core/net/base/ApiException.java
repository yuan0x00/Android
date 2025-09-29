package com.core.net.base;

import androidx.annotation.NonNull;

public class ApiException extends RuntimeException {

    private final int errorCode;

    public ApiException(int errorCode, @NonNull String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
