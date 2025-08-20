package com.example.android.ui.activity.login;

import androidx.lifecycle.ViewModelProvider;

import com.example.android.databinding.ActivityLoginBinding;
import com.example.core.base.BaseActivity;

public class LoginActivity extends BaseActivity<LoginViewModel, ActivityLoginBinding> {

    @Override
    protected LoginViewModel createViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @Override
    protected ActivityLoginBinding createViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }
}