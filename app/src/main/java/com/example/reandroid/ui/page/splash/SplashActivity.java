package com.example.reandroid.ui.page.splash;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.reandroid.R;
import com.example.reandroid.databinding.ActivitySplashBinding;
import com.example.reandroid.ui.base.BaseActivity;
import com.example.reandroid.utils.ToastUtils;

@SuppressLint("CustomSplashScreen")
@Route(path = "/app/splash")
public class SplashActivity extends BaseActivity {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getRoot().postDelayed(() -> {
            binding.tvText.setText(getResources().getString(R.string.splash_done));
            ARouter.getInstance().build("/app/main").navigation();
            ToastUtils.showShortToast("跳转首页");
            finish();
        }, 1500);
    }
}