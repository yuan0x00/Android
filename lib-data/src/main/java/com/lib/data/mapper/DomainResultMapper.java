package com.lib.data.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.network.base.BaseResponse;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;

/**
 * 将网络层响应转换为领域层结果的通用工具。
 */
public final class DomainResultMapper {

    private DomainResultMapper() {
    }

    @NonNull
    public static <T> DomainResult<T> map(@Nullable BaseResponse<T> response) {
        if (response == null) {
            return DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE, "响应为空"));
        }
        if (response.isSuccess()) {
            T data = response.getData();
            if (data != null) {
                return DomainResult.success(data);
            }
            return DomainResult.failure(DomainError.of(response.getErrorCode(), "响应数据为空"));
        }
        return DomainResult.failure(DomainError.of(response.getErrorCode(), response.getErrorMsg()));
    }

    @NonNull
    public static <T> DomainResult<T> mapError(@NonNull Throwable throwable) {
        return DomainResult.<T>failure(DomainError.from(throwable));
    }
}
