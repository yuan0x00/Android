package com.core;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.core.common.data.StorageManager;
import com.core.common.device.ScreenAdaptUtils;
import com.core.common.lifecycle.AppLifecycleObserver;
import com.core.common.log.Logger;
import com.tencent.mmkv.MMKV;

/**
 * 应用程序基础类
 * 提供全局上下文、初始化基础组件等功能
 */
public class CoreApp extends Application {

    private static CoreApp sInstance;
    private volatile boolean isInitialized = false;

    /**
     * 获取全局 Application Context
     * 线程安全，推荐所有工具类使用此 Context
     *
     * @return Application Context
     * @throws IllegalStateException 如果 CoreApp 尚未初始化
     */
    @NonNull
    public static Context getAppContext() {
        if (sInstance == null) {
            throw new IllegalStateException("CoreApp not initialized yet! Please make sure your Application class extends CoreApp.");
        }
        return sInstance.getApplicationContext();
    }

    /**
     * 获取 CoreApp 实例（如需调用非静态方法）
     *
     * @return CoreApp 实例
     * @throws IllegalStateException 如果 CoreApp 尚未初始化
     */
    @NonNull
    public static CoreApp getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("CoreApp not initialized yet! Please make sure your Application class extends CoreApp.");
        }
        return sInstance;
    }

    /**
     * 检查 CoreApp 是否已初始化
     *
     * @return true 如果已初始化，否则返回 false
     */
    public static boolean isAppInitialized() {
        return sInstance != null;
    }

    /**
     * 获取 CoreApp 实例（如果已初始化）
     *
     * @return CoreApp 实例或 null（如果未初始化）
     */
    @Nullable
    public static CoreApp getInstanceOrNull() {
        return sInstance;
    }

    /**
     * 获取初始化状态
     * 
     * @return true 如果应用已初始化，否则返回 false
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 设置单例实例
        sInstance = this;
        
        // 执行初始化
        performInitialization();
    }

    /**
     * 执行核心组件初始化
     * 保证初始化顺序和错误处理
     */
    private void performInitialization() {
        try {
            // 1. 初始化日志系统（最先初始化，以便记录后续步骤的日志）
            Logger.init(BuildConfig.DEBUG);
            Logger.d("CoreApp", "Logger initialized");

            // 2. 初始化全局崩溃处理器（可选）
            // 如需启用崩溃监控，请取消注释下一行
            // GlobalCrashHandler.install();
            Logger.d("CoreApp", "GlobalCrashHandler installation skipped (commented out)");

            // 3. 初始化 MMKV（键值存储）
            String rootDir = MMKV.initialize(this);
            Logger.d("CoreApp", "MMKV initialized at: " + rootDir);
            
            // 4. 初始化存储管理器
            StorageManager.init();
            Logger.d("CoreApp", "StorageManager initialized");

            // 5. 初始化屏幕适配工具
            ScreenAdaptUtils.init(this);
            Logger.d("CoreApp", "ScreenAdaptUtils initialized");

            // 6. 初始化应用生命周期观察者
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver());
            Logger.d("CoreApp", "AppLifecycleObserver added");

            // 标记初始化完成
            isInitialized = true;
            Logger.i("CoreApp", "CoreApp initialization completed successfully");
            
        } catch (Exception e) {
            Logger.e("CoreApp", "Error during CoreApp initialization", e);
            // 在崩溃处理器安装后，异常会被记录，但仍抛出以确保问题被发现
            throw new RuntimeException("Failed to initialize CoreApp", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 应用终止时的清理工作
        isInitialized = false;
        Logger.d("CoreApp", "CoreApp terminated");
    }
}
