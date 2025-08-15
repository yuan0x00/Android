package com.example.reandroid.ui.activity.splash;

import android.annotation.SuppressLint;

import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.core.base.BaseActivity;
import com.example.core.utils.ToastUtils;
import com.example.reandroid.R;
import com.example.reandroid.databinding.ActivitySplashBinding;

@SuppressLint("CustomSplashScreen")
@Route(path = "/app/splash")
public class SplashActivity extends BaseActivity<SplashViewModel, ActivitySplashBinding> {

    @Override
    protected SplashViewModel createViewModel() {
        return new ViewModelProvider(this).get(SplashViewModel.class);
    }

    @Override
    protected ActivitySplashBinding createViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        binding.getRoot().postDelayed(() -> {
            binding.tvText.setText(getResources().getString(R.string.splash_done));
            ARouter.getInstance().build("/app/main").navigation();
            ToastUtils.showShortToast("跳转首页");
            finish();
        }, 1500);
    }
}