package com.rapid.android.init;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.Task;
import com.rapid.android.init.tasks.*;

import java.util.List;

public class MainApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public List<Task> addInitTasks() {
        return List.of(
                new NetworkTask(),
                new StrictModeTask(),
                new ThemeTask(),
                new RouterTask(),
                new AnalyticsTask()
        );
    }

    @Override
    public void onAppInitialized() {
//        GlobalCrashHandler.setCrashReporter(new AppCrashReporter());
    }

}
