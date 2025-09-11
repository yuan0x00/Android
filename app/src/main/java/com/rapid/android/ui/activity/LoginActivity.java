package com.rapid.android.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseActivity;
import com.core.utils.ui.ToastUtils;
import com.rapid.android.databinding.ActivityLoginBinding;
import com.rapid.android.viewmodel.LoginViewModel;

public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {

    private Toolbar toolbar;

    @Override
    protected LoginViewModel createViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Override
    protected ActivityLoginBinding createViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.inputAccount.getText().toString().trim();
            String password = binding.inputPassword.getText().toString().trim();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                ToastUtils.showLongToast("请输入用户名和密码");
                return;
            }
            binding.btnLogin.setEnabled(false);
            viewModel.login(username, password);
        });

        if (viewModel.isLoggedIn()) {
            finish();
        }
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            binding.btnLogin.setEnabled(true);
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showLongToast("登录成功");
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            binding.btnLogin.setEnabled(true);
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
    }

    @Override
    protected void initializeViews() {
        setupToolbar();
    }

    private void setupToolbar() {
        toolbar = findViewById(com.webview.R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("登录或注册");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void loadData() {
        // 无需硬编码登录
    }
}
