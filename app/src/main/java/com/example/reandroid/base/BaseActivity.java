package com.example.reandroid.base;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;
import com.example.reandroid.network.NetworkStateManager;
import com.example.reandroid.utils.AdaptScreenUtils;
import com.example.reandroid.utils.BarUtils;
import com.example.reandroid.utils.ScreenUtils;

public abstract class BaseActivity<VB extends ViewBinding, VM extends ViewModel> extends AppCompatActivity {

    protected VB mBinding;
    protected VM mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);
        BarUtils.setStatusBarLightMode(this, true);
        super.onCreate(savedInstanceState);

        getLifecycle().addObserver(NetworkStateManager.getInstance());

        mBinding = inflateViewBinding(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(getViewModelClass());
    }

    @NonNull
    protected abstract VB inflateViewBinding(@NonNull LayoutInflater inflater);

    @NonNull
    protected abstract Class<VM> getViewModelClass();

    @Override
    public Resources getResources() {
        if (ScreenUtils.isPortrait()) {
            return AdaptScreenUtils.adaptWidth(super.getResources(), 360);
        } else {
            return AdaptScreenUtils.adaptHeight(super.getResources(), 640);
        }
    }

}
