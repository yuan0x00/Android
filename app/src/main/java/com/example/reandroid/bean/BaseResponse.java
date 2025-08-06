package com.example.reandroid.bean;

import androidx.annotation.Keep;

import kotlinx.serialization.Serializable;

@Keep
@Serializable
public class BaseResponse<T> {

    private int errorCode = 0;
    private String errorMsg = "";
    private T data;

    // 无参构造（Gson 需要）
    public BaseResponse() {
    }

    // 全参构造（可选）
    public BaseResponse(int errorCode, String errorMsg, T data) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
    }

    // Getter 方法
    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public T getData() {
        return data;
    }

    // Setter 方法（Retrofit + Gson 通常不需要，但有时用于调试或修改）
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setData(T data) {
        this.data = data;
    }

    // toString 方法（可选，便于调试）
    @Override
    public String toString() {
        return "BaseResponse{" +
                "errorCode=" + errorCode +
                ", errorMsg='" + errorMsg + '\'' +
                ", data=" + data +
                '}';
    }
}