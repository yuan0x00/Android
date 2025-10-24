package com.rapid.android.core.common.app.init.tasks;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.InitTask;
import com.rapid.android.core.common.app.init.TaskType;
import com.rapid.android.core.datastore.AuthStorage;
import com.rapid.android.core.datastore.DefaultDataStore;
import com.rapid.android.core.datastore.IDataStore;
import com.rapid.android.core.log.LogKit;
import com.tencent.mmkv.MMKV;

public class StorageTask extends InitTask {
    private static final String TAG = "StorageTask";

    @Override
    public String getName() {
        return "Storage";
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
    public boolean isSyncMethod() {
        return true;
    }

    @Override
    public void execute() throws Exception {
        String rootDir = MMKV.initialize(BaseApplication.getAppContext());
        LogKit.d(TAG, "MMKV initialized at: %s", rootDir);
        // 初始化AuthStorage
        IDataStore dataStore = new DefaultDataStore();
        AuthStorage.init(dataStore);
    }
}
