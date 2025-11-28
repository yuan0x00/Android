package com.rapid.android.init.tasks;

import com.google.android.material.color.DynamicColors;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.store.ThemeStore;

public class ThemeTask extends Task {

    @Override
    public void run() {
        ThemeStore.applySavedTheme();
        DynamicColors.applyToActivitiesIfAvailable(BaseApplication.getInstance());
    }
}
