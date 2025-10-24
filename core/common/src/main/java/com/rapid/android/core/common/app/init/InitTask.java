package com.rapid.android.core.common.app.init;

import java.util.Collections;
import java.util.List;

public abstract class InitTask implements Task {
    @Override
    public List<String> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.NORMAL;
    }

    @Override
    public boolean isMainThread() {
        return false;
    }

    @Override
    public boolean isSyncMethod() {
        return false;
    }
}
