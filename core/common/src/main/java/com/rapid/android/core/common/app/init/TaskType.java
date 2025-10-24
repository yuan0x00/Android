package com.rapid.android.core.common.app.init;

public enum TaskType {
    CRITICAL,       // 关键任务 - 必须立即执行，阻塞应用启动
    NORMAL,         // 普通任务 - 正常执行
    DELAYED,        // 延迟任务 - 在关键任务完成后执行
    LAZY            // 懒加载任务 - 在应用空闲时执行
}
