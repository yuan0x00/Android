package com.rapid.android.init;

import com.rapid.android.core.common.app.BaseApplication;

public class MainApplication extends BaseApplication {

    private AppStartup appStartup;

    @Override
    public void onCreate() {
        super.onCreate();
        appStartup = new AppStartup(this);
        appStartup.initialize();
    }

    @Override
    public void onTerminate() {
        if (appStartup != null) {
            appStartup.onTerminate();
        }
        super.onTerminate();
    }
}
