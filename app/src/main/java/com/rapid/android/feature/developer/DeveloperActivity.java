package com.rapid.android.feature.developer;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastViewUtils;
import com.rapid.android.databinding.ActivityDeveloperBinding;

public class DeveloperActivity extends BaseActivity<DeveloperViewModel, ActivityDeveloperBinding> {

    @Override
    protected DeveloperViewModel createViewModel() {
        return new ViewModelProvider(this).get(DeveloperViewModel.class);
    }

    @Override
    protected ActivityDeveloperBinding createViewBinding(View rootView) {
        return ActivityDeveloperBinding.bind(rootView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_developer;
    }

    @Override
    protected void initializeViews() {
        setupToolbar();
        setupListeners();
    }

    @Override
    protected void setupObservers() {
        viewModel.getHost().observe(this, (host)->{
            binding.editHost.setText(host);
        });
        viewModel.getPort().observe(this, (port)->{
            binding.editPort.setText(String.valueOf(port));
        });
        viewModel.getProxyEnable().observe(this, (proxyEnable)->{
            binding.switchEnableProxy.setChecked(proxyEnable);
        });
        viewModel.getMessageRes().observe(this, resId -> {
            if (resId != null) {
                showShortToast(getString(resId));
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.switchEnableProxy.setOnCheckedChangeListener((buttonView, isChecked) -> updateInputEnabled(isChecked));

        binding.btnSave.setOnClickListener(v -> viewModel.saveProxyConfig(
                binding.switchEnableProxy.isChecked(),
                getTextValue(binding.editHost),
                getTextValue(binding.editPort)
        ));
    }

    private void updateInputEnabled(boolean proxyEnabled) {
        binding.inputLayoutHost.setEnabled(proxyEnabled);
        binding.inputLayoutPort.setEnabled(proxyEnabled);
        binding.editHost.setEnabled(proxyEnabled);
        binding.editPort.setEnabled(proxyEnabled);
    }

    private String getTextValue(@Nullable com.google.android.material.textfield.TextInputEditText editText) {
        return editText != null && editText.getText() != null
                ? editText.getText().toString()
                : "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShortToast(String message) {
        ToastViewUtils.showShortToast(getDialogController(), message);
    }
}
