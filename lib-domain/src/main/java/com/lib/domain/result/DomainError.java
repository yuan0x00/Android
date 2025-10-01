package com.lib.domain.result;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 领域层统一错误描述，避免直接暴露基础设施异常。
 */
public final class DomainError {

    public static final int UNKNOWN_CODE = Integer.MIN_VALUE;
    public static final int NETWORK_UNAVAILABLE_CODE = -1000;

    private final int code;
    private final String message;
    @Nullable
    private final Throwable cause;

    private DomainError(int code, @Nullable String message, @Nullable Throwable cause) {
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

    @NonNull
    public static DomainError of(int code, @Nullable String message) {
        return new DomainError(code, message, null);
    }

    @NonNull
    public static DomainError of(int code, @Nullable String message, @Nullable Throwable cause) {
        return new DomainError(code, message, cause);
    }

    @NonNull
    public static DomainError from(@NonNull Throwable throwable) {
        return new DomainError(UNKNOWN_CODE, throwable.getMessage(), throwable);
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public Throwable getCause() {
        return cause;
    }
}
