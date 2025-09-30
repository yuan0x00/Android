package com.core.network.base;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kotlinx.serialization.Serializable;

@Keep
@Serializable
public class BaseResponse<T> {

    private static int successCode = 0;

    private int errorCode = 0;
    private String errorMsg = "";
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(int errorCode, String errorMsg, T data) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    /**
     * 配置成功码（默认为0）
     * 建议在 Application.onCreate() 中调用
     */
    public static void setSuccessCode(int code) {
        successCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return errorCode == successCode;
    }

    /**
     * 获取数据，如果失败或数据为null则抛出异常
     * @throws ApiException 当请求失败或数据为null时
     */
    @NonNull
    public T requireData() {
        if (!isSuccess()) {
            throw new ApiException(errorCode, errorMsg != null ? errorMsg : "Request failed");
        }
        if (data == null) {
            throw new ApiException(errorCode, "Response data is null");
        }
        return data;
    }

    /**
     * 获取数据，如果失败或数据为null则返回默认值
     */
    @NonNull
    public T getDataOrDefault(@NonNull T defaultValue) {
        return (isSuccess() && data != null) ? data : defaultValue;
    }

    @NonNull
    @Override
    public String toString() {
        return "BaseResponse{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", data=" + data +
                '}';
    }
}
