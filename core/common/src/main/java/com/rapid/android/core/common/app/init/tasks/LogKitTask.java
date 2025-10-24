package com.rapid.android.core.common.app.init.tasks;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.common.app.init.AsyncTask;
import com.rapid.android.core.log.LogKit;

public class LogKitTask extends AsyncTask {
    @Override
    public String getName() {
        return "LogKit";
    }

    @Override
    public void execute() throws Exception {
        LogKit.init(BuildConfig.DEBUG);
        LogKit.d("BaseApplication", "LogKit initialized");
    }
}
