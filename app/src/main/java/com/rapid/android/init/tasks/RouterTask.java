package com.rapid.android.init.tasks;

import com.rapid.android.core.common.app.init.AsyncTask;
import com.rapid.android.navigation.AppRouter;

public class RouterTask extends AsyncTask {
    @Override
    public String getName() {
        return "Router";
    }

    @Override
    public void execute() throws Exception {
        AppRouter.init();
    }
}
