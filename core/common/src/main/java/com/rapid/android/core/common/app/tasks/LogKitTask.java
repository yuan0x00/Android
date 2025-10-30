package com.rapid.android.core.common.app.tasks;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;
import com.rapid.android.core.log.LogKit;

public class LogKitTask extends Task {
    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        LogKit.init(BuildConfig.DEBUG);
    }
}
