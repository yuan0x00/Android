package com.rapid.android.init;

import android.os.Looper;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.init.tasks.*;
import com.rapid.compose.core.webview.core.WebViewManager;

import java.util.List;

public class MainApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        new NetworkTask().run();
    }

    @Override
    public List<Task> addInitTasks() {
        return List.of(
                new SessionTask(),
                new StrictModeTask(),
                new ThemeTask(),
                new RouterTask(),
                new AnalyticsTask()
        );
    }

    @Override
    public void onAppInitialized() {
        onIdleHandler();
    }

    public void onIdleHandler() {
        Looper.myQueue().addIdleHandler(() -> {
            WebViewManager.getInstance().initPool(this);
            return false; // 只执行一次
        });
    }

}
