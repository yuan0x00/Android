package com.example.reandroid.ui.page.main;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.example.reandroid.R;
import com.example.reandroid.ui.base.BaseActivity;
import com.example.reandroid.utils.ToastUtils;

public class MainActivity extends BaseActivity {

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取 ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // 观察结果
        observeLoginResult();

        // 点击事件
        findViewById(R.id.tv_text).setOnClickListener(v -> viewModel.login("123", "123"));
    }

    private void observeLoginResult() {
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showLongToast("登录成功");
                // 跳转页面等
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
    }
}