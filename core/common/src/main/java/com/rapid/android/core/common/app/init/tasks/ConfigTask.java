package com.rapid.android.core.common.app.init.tasks;

import android.util.Log;

import com.rapid.android.core.common.app.init.AsyncTask;
import com.rapid.android.core.common.app.init.TaskType;

public class ConfigTask extends AsyncTask {

    @Override
    public String getName() {
        return "Config";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.CRITICAL;
    }

    @Override
    public void execute() throws Exception {
        Log.d("ConfigInit", "Loading configuration...");
        Log.d("ConfigInit", "Configuration loaded");
    }
}
