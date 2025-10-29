package com.rapid.android.core.common.app.init.tasks;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.Task;
import com.rapid.android.core.common.app.init.TaskType;
import com.rapid.android.core.datastore.AuthStorage;
import com.rapid.android.core.datastore.DefaultDataStore;
import com.tencent.mmkv.MMKV;

public class StorageTask extends Task {

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        String rootDir = MMKV.initialize(BaseApplication.getAppContext());
        AuthStorage.init(new DefaultDataStore());
    }
}
