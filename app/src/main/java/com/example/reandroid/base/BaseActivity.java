package com.example.reandroid.base;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reandroid.network.NetworkStateManager;
import com.example.reandroid.utils.AdaptScreenUtils;
import com.example.reandroid.utils.BarUtils;
import com.example.reandroid.utils.ScreenUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);
        BarUtils.setStatusBarLightMode(this, true);

        super.onCreate(savedInstanceState);

        getLifecycle().addObserver(NetworkStateManager.getInstance());
    }

    @Override
    public Resources getResources() {
        if (ScreenUtils.isPortrait()) {
            return AdaptScreenUtils.adaptWidth(super.getResources(), 360);
        } else {
            return AdaptScreenUtils.adaptHeight(super.getResources(), 640);
        }
    }

}
