package com.example.reandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.example.reandroid.base.BaseActivity;
import com.example.reandroid.databinding.ActivitySplashBinding;
import com.example.reandroid.utils.ToastUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity<ActivitySplashBinding, SplashViewModel> extends BaseActivity {

    @Override
    protected ActivitySplashBinding inflateViewBinding(LayoutInflater inflater) {
        return ActivitySplashBinding.inflate(inflater);
    }

    @Override
    protected Class<SplashViewModel> getViewModelClass() {
        return SplashActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding.getRoot().postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
//            mBinding.tvText.setText(getResources().getString(R.string.splash_done));
            ToastUtils.showShortToast("跳转首页");
            finish();
        }, 1500);
    }
}