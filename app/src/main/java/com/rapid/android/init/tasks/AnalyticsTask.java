package com.rapid.android.init.tasks;

import com.rapid.android.analytics.AnalyticsInitializer;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.InitTask;

public class AnalyticsTask extends InitTask {
    @Override
    public String getName() {
        return "Analytics";
    }

    @Override
    public void execute() throws Exception {
        AnalyticsInitializer.init(BaseApplication.getAppContext());
    }
}
