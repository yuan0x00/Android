package com.rapid.android.core.common.app.init.tasks;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.common.app.init.Task;
import com.rapid.android.core.common.app.init.TaskType;
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
