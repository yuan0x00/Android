package com.rapid.android.ui.feature.setting.developer;

import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ActivityProxyConfigBinding;
import com.rapid.android.network.proxy.ProxySettings;

import java.text.DateFormat;
import java.util.Date;

public class ProxyConfigActivity extends BaseActivity<ProxyConfigViewModel, ActivityProxyConfigBinding> {

    private static final String DEFAULT_PROXY_HOST = "192.168.1.1";
    private static final int DEFAULT_PROXY_PORT = 9090;

    @Override
    protected ProxyConfigViewModel createViewModel() {
        return new ViewModelProvider(this).get(ProxyConfigViewModel.class);
    }

    @Override
    protected ActivityProxyConfigBinding createViewBinding() {
        return ActivityProxyConfigBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setupToolbar();
        setupListeners();
        applyDefaultIfNeeded();
    }

    @Override
    protected void setupObservers() {
        viewModel.getSettings().observe(this, this::renderSettings);
        viewModel.getFormError().observe(this, this::renderFormErrors);
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
                getTextValue(binding.editPort),
                binding.switchAutoDisable.isChecked()
        ));
    }

    private void renderSettings(@NonNull ProxySettings settings) {
        if (binding.switchEnableProxy.isChecked() != settings.isEnabled()) {
            binding.switchEnableProxy.setChecked(settings.isEnabled());
        }

        updateInputEnabled(settings.isEnabled());

        String targetHost = settings.getHost();
        if (!TextUtils.isEmpty(targetHost)) {
            CharSequence hostText = binding.editHost.getText();
            String currentHost = hostText != null ? hostText.toString() : "";
            if (!TextUtils.equals(currentHost, targetHost)) {
                binding.editHost.setText(targetHost);
            }
        }

        if (settings.getPort() > 0) {
            String targetPort = String.valueOf(settings.getPort());
            CharSequence portText = binding.editPort.getText();
            String currentPort = portText != null ? portText.toString() : "";
            if (!TextUtils.equals(currentPort, targetPort)) {
                binding.editPort.setText(targetPort);
            }
        }

        if (binding.switchAutoDisable.isChecked() != settings.isAutoDisableOnFailure()) {
            binding.switchAutoDisable.setChecked(settings.isAutoDisableOnFailure());
        }

        updateAutoDisableEnabled(settings.isEnabled());

        binding.tvLastFailure.setText(buildFailureMessage(settings));
    }

    private void renderFormErrors(ProxyConfigViewModel.FormError formError) {
        if (formError == null) {
            binding.inputLayoutHost.setError(null);
            binding.inputLayoutPort.setError(null);
            return;
        }

        Integer hostError = formError.getHostErrorRes();
        Integer portError = formError.getPortErrorRes();

        binding.inputLayoutHost.setError(hostError != null ? getString(hostError) : null);
        binding.inputLayoutPort.setError(portError != null ? getString(portError) : null);
    }

    private void updateInputEnabled(boolean proxyEnabled) {
        binding.inputLayoutHost.setEnabled(proxyEnabled);
        binding.inputLayoutPort.setEnabled(proxyEnabled);
        binding.editHost.setEnabled(proxyEnabled);
        binding.editPort.setEnabled(proxyEnabled);
        updateAutoDisableEnabled(proxyEnabled);
    }

    private void updateAutoDisableEnabled(boolean proxyEnabled) {
        binding.switchAutoDisable.setEnabled(proxyEnabled);
    }

    private void applyDefaultIfNeeded() {
        ProxySettings settings = viewModel.getSettings().getValue();

        if ((settings == null || TextUtils.isEmpty(settings.getHost()))
                && TextUtils.isEmpty(getTextValue(binding.editHost))) {
            binding.editHost.setText(DEFAULT_PROXY_HOST);
        }

        if ((settings == null || settings.getPort() <= 0)
                && TextUtils.isEmpty(getTextValue(binding.editPort))) {
            binding.editPort.setText(String.valueOf(DEFAULT_PROXY_PORT));
        }
    }

    private String buildFailureMessage(@NonNull ProxySettings settings) {
        long timestamp = settings.getLastFailureTimestamp();
        if (timestamp <= 0L) {
            return getString(R.string.proxy_config_last_failure_none);
        }
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        StringBuilder builder = new StringBuilder();
        builder.append(getString(R.string.proxy_config_last_failure_prefix));
        builder.append(format.format(new Date(timestamp)));
        String reason = settings.getLastFailureReason();
        if (!TextUtils.isEmpty(reason)) {
            builder.append(' ').append('(').append(reason).append(')');
        }
        return builder.toString();
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
        ToastUtils.showShortToast(getDialogController(), message);
    }
}
