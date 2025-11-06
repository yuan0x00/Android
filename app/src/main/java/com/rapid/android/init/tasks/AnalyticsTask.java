package com.rapid.android.init.tasks;

import com.rapid.android.BuildConfig;
import com.rapid.android.core.analytics.capture.ExceptionCapture;
import com.rapid.android.core.analytics.monitor.BlockMonitor;
import com.rapid.android.core.analytics.tracker.CommonFields;
import com.rapid.android.core.analytics.tracker.Tracker;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;

public class AnalyticsTask extends Task {

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) {
            BlockMonitor.getInstance().start();
        }
        ExceptionCapture.init();
        // 初始化 SDK
        CommonFields common = new CommonFields("", "", "");
        Tracker.init(common);
    }
}









