package com.example.reandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import com.example.reandroid.base.BaseActivity;
import com.example.reandroid.databinding.ActivitySplashBinding;
import com.example.reandroid.utils.ToastUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.getRoot().postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            binding.tvText.setText(getResources().getString(R.string.splash_done));
            ToastUtils.showShortToast("跳转首页");
            finish();
        }, 1500);
    }
}