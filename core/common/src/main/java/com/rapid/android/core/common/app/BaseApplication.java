package com.rapid.android.core.common.app;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.rapid.android.core.common.app.tasks.LogKitTask;
import com.rapid.android.core.common.app.tasks.StorageTask;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskManager;

import java.util.List;


public abstract class BaseApplication extends Application {

    private static BaseApplication sInstance;
    private TaskManager initManager;

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
//        try {
//            GlobalCrashHandler.install(this);
//            LogKit.d("BaseApplication", "GlobalCrashHandler installed");
//
//            AppLifecycleObserver.initialize(this);
//            ProcessLifecycleOwner.get().getLifecycle().addObserver(AppLifecycleObserver.getInstance());
//            LogKit.d("BaseApplication", "AppLifecycleObserver added");
//
//        } catch (Exception e) {
//            LogKit.e("BaseApplication", e, "Initialization error");
//            throw new RuntimeException("Failed to initialize BaseApplication", e);
//        }

        initManager = new TaskManager(this);
        // 添加初始化任务
        initManager.addTasks(
                List.of(
                        new LogKitTask(),
                        new StorageTask()
                )
        );
        initManager.addTasks(addInitTasks());
        // 启动异步初始化
        initManager.start();
    }

    public abstract List<Task> addInitTasks();

    public abstract void onAppInitialized();

}
