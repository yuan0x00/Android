package com.rapid.android.core.common.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.rapid.android.core.common.app.init.InitTask;
import com.rapid.android.core.common.app.init.TaskManager;
import com.rapid.android.core.common.app.init.tasks.ConfigTask;
import com.rapid.android.core.common.app.init.tasks.LogKitTask;
import com.rapid.android.core.common.app.init.tasks.StorageTask;
import com.rapid.android.core.log.LogKit;

import java.util.List;


public abstract class BaseApplication extends Application {

    private static BaseApplication sInstance;

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

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        performInitialization();
    }

    private void performInitialization() {
        TaskManager initManager = new TaskManager();

        try {
//            GlobalCrashHandler.install(this);
//            LogKit.d("BaseApplication", "GlobalCrashHandler installed");
//
//            AppLifecycleObserver.initialize(this);
//            ProcessLifecycleOwner.get().getLifecycle().addObserver(AppLifecycleObserver.getInstance());
//            LogKit.d("BaseApplication", "AppLifecycleObserver added");

        } catch (Exception e) {
            LogKit.e("BaseApplication", e, "Initialization error");
            throw new RuntimeException("Failed to initialize BaseApplication", e);
        }

        // 添加初始化任务
        initManager.addTasks(
                List.of(new LogKitTask(),
                        new StorageTask(),
                        new ConfigTask())
        );
        initManager.addTasks(addInitTasks());

        // 启动异步初始化
        initManager.start(new TaskManager.InitCallback() {
            @Override
            public void onProgress(float progress) {
                Log.d("InitProgress", "Progress: " + (progress * 100) + "%");
            }

            @Override
            public void onSuccess() {
                Log.i("AppInit", "All initialization tasks completed successfully");
                onAppInitialized();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e("AppInit", "Initialization failed", error);
                // 处理初始化失败
            }
        });
    }

    public abstract List<InitTask> addInitTasks();

    public abstract void onAppInitialized();

}
