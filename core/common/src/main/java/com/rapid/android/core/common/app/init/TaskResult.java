package com.rapid.android.core.common.app.init;

public class TaskResult {
    private final String taskName;
    private final TaskState state;
    private final long duration;
    private final Throwable error;

    public TaskResult(String taskName, TaskState state, long duration, Throwable error) {
        this.taskName = taskName;
        this.state = state;
        this.duration = duration;
        this.error = error;
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskState getState() {
        return state;
    }

    public long getDuration() {
        return duration;
    }

    public Throwable getError() {
        return error;
    }
}
