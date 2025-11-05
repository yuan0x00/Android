package com.rapid.android.core.common.app.tasks;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;
import com.rapid.android.core.log.LogKit;
import com.tencent.mmkv.MMKV;

public class MmkvTask extends Task {

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        String rootDir = MMKV.initialize(BaseApplication.getAppContext());
        LogKit.i("MMKV", "mmkv root: " + rootDir);
    }
}
