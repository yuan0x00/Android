package com.rapid.android.core.common.app.init;

import java.util.List;

public abstract class Task implements Runnable {

    // 任务类型
    public TaskType getTaskType(){
        return TaskType.ASYNC;
    };

    // 依赖的任务列表
    public List<Class<? extends Task>> getDependencies() {
        return List.of();
    }

    // 默认名称
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public abstract void run();

    public Integer getEstimatedDuration(){
        return 0;
    }
}
