package com.rapid.android.init.tasks;

import com.rapid.android.core.common.app.init.AsyncTask;
import com.rapid.android.core.data.DataInitializer;

import java.util.List;

public class AuthStorageTask extends AsyncTask {
    @Override
    public String getName() {
        return "AuthStorage";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("Storage");
    }

    @Override
    public void execute() throws Exception {
        DataInitializer.init();
    }
}
