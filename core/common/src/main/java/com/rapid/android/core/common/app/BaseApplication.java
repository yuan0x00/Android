package com.rapid.android.core.common.app;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.common.data.StorageManager;
import com.rapid.android.core.common.device.ScreenAdaptUtils;
import com.rapid.android.core.common.lifecycle.AppLifecycleObserver;
import com.rapid.android.core.log.LogKit;
import com.tencent.mmkv.MMKV;

/**
 * 可复用的基础 Application，实现组件初始化、全局上下文获取等通用能力。
 * 业务层可直接继承此类并在 {@link #onCreate()} 中调用父类逻辑。
 */
public class BaseApplication extends Application {

    private static BaseApplication sInstance;
    private volatile boolean isInitialized = false;

    @NonNull
    public static Context getAppContext() {
        if (sInstance == null) {
            throw new IllegalStateException("BaseApplication not initialized. Ensure your Application extends BaseApplication.");
        }
        return sInstance.getApplicationContext();
    }

    @NonNull
    public static BaseApplication getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("BaseApplication not initialized. Ensure your Application extends BaseApplication.");
        }
        return sInstance;
    }

    @Nullable
    public static BaseApplication getInstanceOrNull() {
        return sInstance;
    }

    public static boolean isAppInitialized() {
        return sInstance != null;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        performInitialization();
    }

    private void performInitialization() {
        try {
            LogKit.init(BuildConfig.DEBUG);
            LogKit.d("BaseApplication", "LogKit initialized");

            // 如需启用崩溃监控，取消下行注释
            // GlobalCrashHandler.install(this);
            LogKit.d("BaseApplication", "GlobalCrashHandler installation skipped (commented out)");

            String rootDir = MMKV.initialize(this);
            LogKit.d("BaseApplication", "MMKV initialized at: %s", rootDir);

            StorageManager.init();
            LogKit.d("BaseApplication", "StorageManager initialized");

            ScreenAdaptUtils.init(this);
            LogKit.d("BaseApplication", "ScreenAdaptUtils initialized");

            AppLifecycleObserver.initialize(this);
            ProcessLifecycleOwner.get().getLifecycle().addObserver(AppLifecycleObserver.getInstance());
            LogKit.d("BaseApplication", "AppLifecycleObserver added");

            isInitialized = true;
            LogKit.i("BaseApplication", "Initialization completed successfully");

        } catch (Exception e) {
            LogKit.e("BaseApplication", e, "Initialization error");
            throw new RuntimeException("Failed to initialize BaseApplication", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        isInitialized = false;
        LogKit.d("BaseApplication", "Application terminated");
    }
}
