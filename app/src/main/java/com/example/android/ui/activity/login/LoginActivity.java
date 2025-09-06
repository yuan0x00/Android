package com.example.android.ui.activity.login;

import androidx.lifecycle.ViewModelProvider;

import com.example.android.databinding.ActivityLoginBinding;
import com.example.core.base.ui.BaseActivity;
import com.example.core.utils.ui.ToastUtils;

public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {

    @Override
    protected LoginViewModel createViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Override
    protected ActivityLoginBinding createViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.login("123", "123");
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showLongToast("登录成功");
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
    }
}