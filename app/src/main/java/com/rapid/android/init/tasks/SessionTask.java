package com.rapid.android.init.tasks;

import com.rapid.android.core.common.app.tasks.MmkvTask;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;

import java.util.List;

public class SessionTask extends Task {

    private static final String TAG = "SessionTask";

    @Override
    public List<Class<? extends Task>> getDependencies() {
        return List.of(MmkvTask.class, NetworkTask.class);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        SessionManager.getInstance().initialize();
    }

}
