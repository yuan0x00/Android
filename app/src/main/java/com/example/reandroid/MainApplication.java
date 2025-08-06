package com.example.reandroid;

import android.app.Application;
import com.example.reandroid.utils.Utils;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
