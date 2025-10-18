package com.rapid.android.ui.feature.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.common.utils.WindowInsetsUtils;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ActivityLoginBinding;

public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {

    private boolean isRegisterMode = false;

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
                ToastUtils.showLongToast(getDialogController(), getString(R.string.login_prompt_credentials));
                return;
            }
            binding.btnLogin.setEnabled(false);
            if (isRegisterMode) {
                String confirm = binding.inputPasswordConfirm.getText() != null
                        ? binding.inputPasswordConfirm.getText().toString().trim()
                        : "";
                if (!TextUtils.equals(password, confirm)) {
                    ToastUtils.showLongToast(getDialogController(), getString(R.string.login_error_password_mismatch));
                    binding.btnLogin.setEnabled(true);
                    return;
                }
                viewModel.register(username, password, confirm);
            } else {
                viewModel.login(username, password);
            }
        });

        binding.btnToggleMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            updateMode();
        });
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            binding.btnLogin.setEnabled(true);
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showLongToast(getDialogController(), getString(R.string.login_success));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            binding.btnLogin.setEnabled(true);
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(getDialogController(), msg);
            }
        });

        viewModel.getInfoMessage().observe(this, msg -> {
            if (!TextUtils.isEmpty(msg)) {
                ToastUtils.showLongToast(getDialogController(), msg);
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
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        WindowInsetsUtils.addImeVisibilityListener(binding.contentContainer, isVisible ->
                binding.layoutPrivacy.setVisibility(isVisible ? android.view.View.GONE : android.view.View.VISIBLE));
        updateMode();
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

    private void updateMode() {
        binding.inputPasswordConfirmLayout.setVisibility(isRegisterMode ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnLogin.setText(isRegisterMode ? R.string.login_action_register : R.string.login_action_login);
        binding.btnToggleMode.setText(isRegisterMode ? R.string.login_toggle_login : R.string.login_toggle_register);
        binding.toolbar.setTitle(isRegisterMode ? R.string.login_action_register : R.string.login_title);
    }

}
