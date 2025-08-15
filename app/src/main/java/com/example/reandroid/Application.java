package com.example.reandroid;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.core.utils.Utils;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        ARouter.init(this);
    }
}
