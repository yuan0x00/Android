package com.lib.domain.result;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 领域层通用结果封装，隔离基础设施响应结构。
 */
public final class DomainResult<T> {

    private final T data;
    @Nullable
    private final DomainError error;

    private DomainResult(@Nullable T data, @Nullable DomainError error) {
        this.data = data;
        this.error = error;
    }

    @NonNull
    public static <T> DomainResult<T> success(@NonNull T data) {
        return new DomainResult<>(Objects.requireNonNull(data), null);
    }

    @NonNull
    public static <T> DomainResult<T> emptySuccess() {
        return new DomainResult<>(null, null);
    }

    @NonNull
    public static <T> DomainResult<T> failure(@NonNull DomainError error) {
        return new DomainResult<>(null, Objects.requireNonNull(error));
    }

    public boolean isSuccess() {
        return error == null;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public DomainError getError() {
        return error;
    }
}
