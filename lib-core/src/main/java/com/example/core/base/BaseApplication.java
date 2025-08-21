package com.example.core.base;

import android.app.Application;

import com.example.core.utils.SPUtils;
import com.example.core.utils.Utils;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        SPUtils.init(this);
    }
}
