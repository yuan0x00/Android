package com.rapid.android.init.tasks;

import com.rapid.android.core.initializer.Task;
import com.rapid.android.navigation.AppRouter;

public class RouterTask extends Task {

    @Override
    public void run() {
        AppRouter.init();
    }
}
