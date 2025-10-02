package com.rapid.android.ui.feature.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.core.common.utils.ToastUtils;
import com.core.ui.dialog.DialogController;
import com.core.ui.dialog.DialogEffect;
import com.core.ui.presentation.BaseActivity;
import com.rapid.android.R;
import com.rapid.android.databinding.ActivitySettingBinding;
import com.rapid.android.utils.ThemeManager;

public class SettingActivity extends BaseActivity<SettingViewModel, ActivitySettingBinding> {

    private DialogController dialogController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
        dialogController = DialogController.from(this, binding.getRoot());
        setupObservers();
        setupClickListeners();
        viewModel.loadSettings();
    }

    @Override
    protected SettingViewModel createViewModel() {
        return new ViewModelProvider(this).get(SettingViewModel.class);
    }

    @Override
    protected ActivitySettingBinding createViewBinding() {
        return ActivitySettingBinding.inflate(getLayoutInflater());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    protected void setupObservers() {

        viewModel.getThemeMode().observe(this, mode -> {
            if (mode == null) {
                return;
            }
            switch (mode) {
                case LIGHT:
                    if (binding.groupThemeMode.getCheckedButtonId() != binding.btnThemeLight.getId()) {
                        binding.groupThemeMode.check(binding.btnThemeLight.getId());
                    }
                    break;
                case DARK:
                    if (binding.groupThemeMode.getCheckedButtonId() != binding.btnThemeDark.getId()) {
                        binding.groupThemeMode.check(binding.btnThemeDark.getId());
                    }
                    break;
                case SYSTEM:
                default:
                    if (binding.groupThemeMode.getCheckedButtonId() != binding.btnThemeSystem.getId()) {
                        binding.groupThemeMode.check(binding.btnThemeSystem.getId());
                    }
                    break;
            }
        });

        viewModel.getDataSaver().observe(this, enabled -> {
            boolean target = Boolean.TRUE.equals(enabled);
            if (binding.switchDataSaver.isChecked() != target) {
                binding.switchDataSaver.setChecked(target);
            }
        });

        viewModel.getWifiOnlyMedia().observe(this, enabled -> {
            boolean target = Boolean.TRUE.equals(enabled);
            if (binding.switchWifiOnly.isChecked() != target) {
                binding.switchWifiOnly.setChecked(target);
            }
        });

        viewModel.getNotifications().observe(this, isChecked -> {
            boolean target = Boolean.TRUE.equals(isChecked);
            if (binding.switchNotifications.isChecked() != target) {
                binding.switchNotifications.setChecked(target);
            }
        });

        // 观察操作消息
        viewModel.getOperationMessageRes().observe(this, messageRes -> {
            if (messageRes != null) {
                ToastUtils.showShortToast(getString(messageRes));
            }
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            // 可以在这里显示/隐藏加载指示器
            if (isLoading) {
                // 显示加载指示器
            } else {
                // 隐藏加载指示器
            }
        });
    }

    private void setupClickListeners() {

        binding.groupThemeMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == binding.btnThemeSystem.getId()) {
                viewModel.setThemeMode(ThemeManager.ThemeMode.SYSTEM);
            } else if (checkedId == binding.btnThemeLight.getId()) {
                viewModel.setThemeMode(ThemeManager.ThemeMode.LIGHT);
            } else if (checkedId == binding.btnThemeDark.getId()) {
                viewModel.setThemeMode(ThemeManager.ThemeMode.DARK);
            }
        });

        binding.switchDataSaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            viewModel.setDataSaver(isChecked);
        });

        binding.switchWifiOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            viewModel.setWifiOnlyMedia(isChecked);
        });

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            viewModel.setNotifications(isChecked);
        });

        binding.itemClearCache.setOnClickListener(v -> viewModel.clearCache());

        binding.itemPrivacy.setOnClickListener(v ->
                openWebPage(getString(R.string.settings_privacy_url)));

        binding.itemOpenSource.setOnClickListener(v ->
                openWebPage(getString(R.string.settings_open_source_url)));

        binding.btnLogout.setOnClickListener(v -> dialogController.show(
                new DialogEffect.Confirm(
                        "logout_confirm",
                        getString(R.string.setting_dialog_logout_title),
                        getString(R.string.setting_dialog_logout_message),
                        getString(R.string.confirm),
                        getString(R.string.cancel),
                        () -> viewModel.logoutWithCallback((success, messageRes) -> {
                            ToastUtils.showShortToast(getString(messageRes));
                            if (success) {
                                binding.getRoot().postDelayed(this::finish, 500L);
                            }
                        }),
                        null
                )));
    }

    private void openWebPage(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.setting_open_link_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
