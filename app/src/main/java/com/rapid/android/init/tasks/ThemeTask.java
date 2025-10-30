package com.rapid.android.init.tasks;

import com.google.android.material.color.DynamicColors;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.utils.ThemeManager;

public class ThemeTask extends Task {

    @Override
    public void run() {
        ThemeManager.applySavedTheme();
        DynamicColors.applyToActivitiesIfAvailable(BaseApplication.getInstance());
    }
}
