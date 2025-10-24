package com.rapid.android.core.common.app.init.tasks;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.common.app.init.InitTask;
import com.rapid.android.core.common.app.init.TaskType;
import com.rapid.android.core.log.LogKit;

public class LogKitTask extends InitTask {
    @Override
    public String getName() {
        return "LogKit";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.CRITICAL;
    }

    @Override
    public void execute() throws Exception {
        LogKit.init(BuildConfig.DEBUG);
        LogKit.d("BaseApplication", "LogKit initialized");
    }
}
