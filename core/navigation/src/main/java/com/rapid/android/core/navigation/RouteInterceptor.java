package com.rapid.android.core.navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 路由拦截器接口
 * 用于在导航前执行拦截逻辑（如登录检查、权限验证等）
 */
public interface RouteInterceptor {

    /**
     * 拦截路由
     *
     * @param path   目标路径
     * @param extras 传递的参数
     * @return true 允许继续导航，false 拦截导航
     */
    boolean intercept(@NonNull String path, @Nullable Bundle extras);
}
