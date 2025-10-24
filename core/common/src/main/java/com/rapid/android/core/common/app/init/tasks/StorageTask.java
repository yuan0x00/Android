package com.rapid.android.core.common.app.init.tasks;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.AsyncTask;
import com.rapid.android.core.common.data.StorageManager;
import com.rapid.android.core.log.LogKit;
import com.tencent.mmkv.MMKV;

public class StorageTask extends AsyncTask {
    @Override
    public String getName() {
        return "Storage";
    }

    @Override
    public void execute() throws Exception {
        String rootDir = MMKV.initialize(BaseApplication.getAppContext());
        LogKit.d("BaseApplication", "MMKV initialized at: %s", rootDir);

        StorageManager.init();
        StorageManager.whenInitialized().join();
        LogKit.d("BaseApplication", "StorageManager initialized");
    }
}
