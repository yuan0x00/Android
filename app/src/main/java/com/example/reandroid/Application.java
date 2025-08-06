package com.example.reandroid;

import com.example.reandroid.utils.Utils;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
