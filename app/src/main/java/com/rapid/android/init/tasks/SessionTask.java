package com.rapid.android.init.tasks;

import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;

public class SessionTask extends Task {

    private static final String TAG = "SessionTask";

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        SessionManager.getInstance().initialize();
    }

}
