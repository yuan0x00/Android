package com.rapid.android.init.tasks;

import com.rapid.android.core.common.app.init.InitTask;
import com.rapid.android.navigation.AppRouter;

public class RouterTask extends InitTask {
    @Override
    public String getName() {
        return "Router";
    }

    @Override
    public void execute() throws Exception {
        AppRouter.init();
    }
}
