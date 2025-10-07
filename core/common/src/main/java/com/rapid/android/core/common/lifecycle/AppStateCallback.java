package com.rapid.android.core.common.lifecycle;

/**
 * App 前后台状态回调接口
 */
public interface AppStateCallback {
    void onAppForeground();
    void onAppBackground();
}