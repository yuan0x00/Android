package com.lib.domain.result;

/**
 * 领域层统一错误描述，避免直接暴露基础设施异常。
 */
public final class DomainError {

    public static final int UNKNOWN_CODE = Integer.MIN_VALUE;
    public static final int NETWORK_UNAVAILABLE_CODE = -1000;

    private final int code;
    private final String message;
    private final Throwable cause;

    private DomainError(int code, String message, Throwable cause) {
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

    public static DomainError of(int code, String message) {
        return new DomainError(code, message, null);
    }

    public static DomainError of(int code, String message, Throwable cause) {
        return new DomainError(code, message, cause);
    }

    public static DomainError from(Throwable throwable) {
        return new DomainError(UNKNOWN_CODE, throwable.getMessage(), throwable);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
