package com.rapid.android.core.common.app.init;

import java.util.List;

public interface Task {
    String getName();
    void execute() throws Exception;
    List<String> getDependencies(); // 依赖的任务名称
    int getPriority(); // 优先级 数值越大越先执行
    TaskType getTaskType(); // 任务类型 TaskType
    boolean isMainThread(); // 是否在主线程执行
    boolean isSyncMethod(); // 是同步方法
}
