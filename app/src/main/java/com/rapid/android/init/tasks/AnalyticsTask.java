package com.rapid.android.init.tasks;

import com.rapid.android.analytics.AnalyticsInitializer;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.initializer.Task;

public class AnalyticsTask extends Task {

    @Override
    public void run() {
        AnalyticsInitializer.init(BaseApplication.getAppContext());
    }
}
