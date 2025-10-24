package com.rapid.android.core.common.app.init.tasks;

import android.util.Log;

import com.rapid.android.core.common.app.init.AsyncTask;

public class ConfigTask extends AsyncTask {

    @Override
    public String getName() {
        return "Config";
    }

    @Override
    public int getPriority() {
        return 15; // 最高优先级
    }

    @Override
    public void execute() throws Exception {
        Log.d("ConfigInit", "Loading configuration...");
        Log.d("ConfigInit", "Configuration loaded");
    }
}
