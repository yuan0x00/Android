package com.rapid.android.ui.feature.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.core.common.utils.ToastUtils;
import com.core.ui.presentation.BaseActivity;
import com.lib.data.session.SessionManager;
import com.rapid.android.R;
import com.rapid.android.databinding.ActivityLoginBinding;

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
                ToastUtils.showLongToast(getString(R.string.login_prompt_credentials));
                return;
            }
            binding.btnLogin.setEnabled(false);
            viewModel.login(username, password);
        });
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            binding.btnLogin.setEnabled(true);
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showLongToast(getString(R.string.login_success));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            binding.btnLogin.setEnabled(true);
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });

        SessionManager.getInstance().loginState().observe(this, loggedIn -> {
            if (Boolean.TRUE.equals(loggedIn)) {
                finish();
            }
        });
    }

    @Override
    protected void initializeViews() {
        setupToolbar();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.login_title);
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
        if (Boolean.TRUE.equals(SessionManager.getInstance().loginState().getValue())) {
            finish();
        }
    }
}
