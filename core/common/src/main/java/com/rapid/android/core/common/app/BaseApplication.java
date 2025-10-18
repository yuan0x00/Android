package com.rapid.android.core.common.app;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.common.crash.GlobalCrashHandler;
import com.rapid.android.core.common.data.StorageManager;
import com.rapid.android.core.common.lifecycle.AppLifecycleObserver;
import com.rapid.android.core.log.LogKit;
import com.tencent.mmkv.MMKV;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 可复用的基础 Application，实现组件初始化、全局上下文获取等通用能力。
 * 业务层可直接继承此类并在 {@link #onCreate()} 中调用父类逻辑。
 */
public class BaseApplication extends Application {

    private static BaseApplication sInstance;
    private volatile boolean isInitialized = false;
    private static final ExecutorService BACKGROUND_INIT_EXECUTOR = Executors.newSingleThreadExecutor(new InitThreadFactory());
    private final CompletableFuture<Void> initializationFuture = new CompletableFuture<>();

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

            GlobalCrashHandler.install(this);
            LogKit.d("BaseApplication", "GlobalCrashHandler installed");

            AppLifecycleObserver.initialize(this);
            ProcessLifecycleOwner.get().getLifecycle().addObserver(AppLifecycleObserver.getInstance());
            LogKit.d("BaseApplication", "AppLifecycleObserver added");

            CompletableFuture
                    .runAsync(this::runBackgroundInitializationTasks, BACKGROUND_INIT_EXECUTOR)
                    .whenComplete((unused, throwable) -> {
                        if (throwable != null) {
                            isInitialized = false;
                            LogKit.e("BaseApplication", throwable, "Background initialization failed");
                            initializationFuture.completeExceptionally(throwable);
                        } else {
                            isInitialized = true;
                            LogKit.i("BaseApplication", "Initialization completed successfully");
                            initializationFuture.complete(null);
                        }
                    });


        } catch (Exception e) {
            LogKit.e("BaseApplication", e, "Initialization error");
            throw new RuntimeException("Failed to initialize BaseApplication", e);
        }
    }

    private void runBackgroundInitializationTasks() {
        String rootDir = MMKV.initialize(this);
        LogKit.d("BaseApplication", "MMKV initialized at: %s", rootDir);

        StorageManager.init();
        StorageManager.whenInitialized().join();
        LogKit.d("BaseApplication", "StorageManager initialized");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        isInitialized = false;
        if (!initializationFuture.isDone()) {
            initializationFuture.completeExceptionally(new IllegalStateException("Application terminated"));
        }
        BACKGROUND_INIT_EXECUTOR.shutdownNow();
        LogKit.d("BaseApplication", "Application terminated");
    }

    public CompletableFuture<Void> getInitializationFuture() {
        return initializationFuture;
    }

    private static final class InitThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r, "base-init");
            thread.setDaemon(true);
            return thread;
        }
    }
}
