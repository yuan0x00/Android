package com.rapid.android.init.tasks;

import com.google.android.material.color.DynamicColors;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.InitTask;
import com.rapid.android.utils.ThemeManager;

public class ThemeTask extends InitTask {
    @Override
    public String getName() {
        return "Theme";
    }

    @Override
    public void execute() throws Exception {
        ThemeManager.applySavedTheme();
        DynamicColors.applyToActivitiesIfAvailable(BaseApplication.getInstance());
    }
}
