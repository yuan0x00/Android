package com.rapid.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.rapid.core.crash.GlobalCrashHandler;
import com.rapid.core.lifecycle.AppLifecycleObserver;
import com.rapid.core.log.CoreLogger;
import com.rapid.core.utils.device.ScreenAdaptUtils;
import com.rapid.core.utils.storage.MMKVManager;
import com.tencent.mmkv.MMKV;

public class CoreApp extends Application {

    private static CoreApp sInstance;

    /**
     * 获取全局 Application Context
     * 线程安全，推荐所有工具类使用此 Context
     */
    @NonNull
    public static Context getAppContext() {
        if (sInstance == null) {
            throw new IllegalStateException("CoreApp not initialized yet!");
        }
        return sInstance.getApplicationContext();
    }

    /**
     * 获取 CoreApp 实例（如需调用非静态方法）
     */
    @NonNull
    public static CoreApp getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("CoreApp not initialized yet!");
        }
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // 初始化日志
        CoreLogger.init(BuildConfig.DEBUG);

        // 初始化崩溃监控
        GlobalCrashHandler.install();

        // 初始化 MMKV
        MMKV.initialize(this); // MMKV 自己的初始化
        MMKVManager.init();    // 我们的包装类初始化

        // 初始化屏幕适配（如需）
        ScreenAdaptUtils.init(this);

        // 监听 App 前后台
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver());
    }
}
