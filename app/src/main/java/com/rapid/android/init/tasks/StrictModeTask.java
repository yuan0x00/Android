package com.rapid.android.init.tasks;

import android.os.StrictMode;

import com.rapid.android.BuildConfig;
import com.rapid.android.core.common.app.init.AsyncTask;

public class StrictModeTask extends AsyncTask {
    @Override
    public String getName() {
        return "StrictMode";
    }

    @Override
    public void execute() throws Exception {
        if (!BuildConfig.DEBUG) {
            return;
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectCustomSlowCalls()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build());
    }
}
