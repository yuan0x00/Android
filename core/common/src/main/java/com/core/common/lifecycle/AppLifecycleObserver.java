package com.core.common.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.core.log.LogKit;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听整个 App 的前后台切换（基于 ProcessLifecycleOwner）
 * 使用 DefaultLifecycleObserver（替代已废弃的 LifecycleObserver 接口）
 */
public class AppLifecycleObserver implements DefaultLifecycleObserver {


    // 防抖延迟（毫秒），避免快速切换造成误判
    private static final long DEBOUNCE_DELAY_MS = 500;
    private static volatile AppLifecycleObserver sInstance;
    private final Application application;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    // 回调列表（弱引用防泄漏）
    private final List<AppStateCallback> mCallbacks = new ArrayList<>();
    private boolean mIsAppForeground = true; // 初始认为在前台
    // 防抖 Runnable
    private Runnable mBackgroundDelayRunnable;

    private AppLifecycleObserver(@NonNull Application application) {
        this.application = application;
        registerActivityLifecycleCallbacks();
    }

    public static void initialize(@NonNull Application application) {
        if (sInstance == null) {
            synchronized (AppLifecycleObserver.class) {
                if (sInstance == null) {
                    sInstance = new AppLifecycleObserver(application);
                }
            }
        }
    }

    public static AppLifecycleObserver getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("AppLifecycleObserver not initialized. Call initialize(application) first.");
        }
        return sInstance;
    }

    public static void addCallback(@NonNull AppStateCallback callback) {
        getInstance().mCallbacks.add(callback);
    }

    // ———————— 回调管理 ————————

    public static void removeCallback(@NonNull AppStateCallback callback) {
        getInstance().mCallbacks.remove(callback);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // App 进入前台（任意 Activity 可见）
        mHandler.removeCallbacks(mBackgroundDelayRunnable);
        if (!mIsAppForeground) {
            mIsAppForeground = true;
            LogKit.d("AppLifecycle", ">>> App 进入前台");
            notifyAppForeground();
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // App 进入后台（所有 Activity 不可见）
        mBackgroundDelayRunnable = () -> {
            if (mIsAppForeground) {
                mIsAppForeground = false;
                LogKit.d("AppLifecycle", ">>> App 进入后台");
                notifyAppBackground();
            }
        };
        mHandler.postDelayed(mBackgroundDelayRunnable, DEBOUNCE_DELAY_MS);
    }

    // ———————— 单例管理（确保只注册一次） ————————

    private void notifyAppForeground() {
        for (AppStateCallback callback : new ArrayList<>(mCallbacks)) {
            callback.onAppForeground();
        }
    }

    private void notifyAppBackground() {
        for (AppStateCallback callback : new ArrayList<>(mCallbacks)) {
            callback.onAppBackground();
        }
    }

    // ———————— 可选：注册 Activity 生命周期回调，增强判断准确性 ————————

    private void registerActivityLifecycleCallbacks() {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }
}
