package com.example.android.ui.activity.splash;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.lifecycle.ViewModelProvider;

import com.example.android.R;
import com.example.android.databinding.ActivitySplashBinding;
import com.example.android.ui.activity.main.MainActivity;
import com.example.core.base.BaseActivity;

@SuppressLint("CustomSplashScreen")
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
//        long delayMillis = 1000;
        long delayMillis = 0;
        //模拟耗时
        binding.getRoot().postDelayed(() -> {
            binding.tvText.setText(getResources().getString(R.string.splash_done));
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }, delayMillis);
    }
}