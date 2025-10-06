package com.core.domain.result;

import java.util.Objects;

/**
 * 领域层通用结果封装，隔离基础设施响应结构。
 */
public final class DomainResult<T> {

    private final T data;
    private final DomainError error;

    private DomainResult(T data, DomainError error) {
        this.data = data;
        this.error = error;
    }

    public static <T> DomainResult<T> success(T data) {
        return new DomainResult<>(Objects.requireNonNull(data), null);
    }

    public static <T> DomainResult<T> emptySuccess() {
        return new DomainResult<>(null, null);
    }

    public static <T> DomainResult<T> failure(DomainError error) {
        return new DomainResult<>(null, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return error == null;
    }

    public T getData() {
        return data;
    }

    public DomainError getError() {
        return error;
    }
}
