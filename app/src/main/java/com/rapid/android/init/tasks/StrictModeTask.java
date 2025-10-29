package com.rapid.android.init.tasks;

import android.os.StrictMode;

import com.rapid.android.BuildConfig;
import com.rapid.android.core.common.app.init.Task;

public class StrictModeTask extends Task {

    @Override
    public void run() {
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
