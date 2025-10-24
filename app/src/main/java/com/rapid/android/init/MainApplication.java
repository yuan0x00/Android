package com.rapid.android.init;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.InitTask;
import com.rapid.android.init.tasks.*;

import java.util.List;

public class MainApplication extends BaseApplication {

    @Override
    public List<InitTask> addInitTasks() {
        return List.of(
                new ThemeTask(),
                new WebviewTask(),
                new RouterTask(),
                new AnalyticsTask(),
                new StrictModeTask(),
                new NetworkTask(),
                new AuthStorageTask()
        );
    }

    @Override
    public void onAppInitialized() {
//        GlobalCrashHandler.setCrashReporter(new AppCrashReporter());
    }

}
