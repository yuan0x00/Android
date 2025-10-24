package com.rapid.android.core.common.app.init;

import java.util.List;

public interface InitTask {
    String getName();
    void execute() throws Exception;
    List<String> getDependencies(); // 依赖的任务名称
    int getPriority(); // 优先级 数值越大越先执行
    boolean isMainThread(); // 是否在主线程执行
    boolean isCritical(); // 是否为关键人物（关键任务失败会终止初始化）
}
