package com.rapid.android.feature.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.permission.NotificationPermissionManager;
import com.rapid.android.core.ui.components.dialog.DialogEffect;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ActivitySettingBinding;
import com.rapid.android.utils.AppPreferences;
import com.rapid.android.utils.ThemeManager;

public class SettingActivity extends BaseActivity<SettingViewModel, ActivitySettingBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
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

        viewModel.getNotifications().observe(this, isChecked -> {
            boolean target = Boolean.TRUE.equals(isChecked);
            if (binding.switchNotifications.isChecked() != target) {
                binding.switchNotifications.setChecked(target);
            }
        });

        viewModel.getHomeTopEnabled().observe(this, isChecked -> {
            boolean target = Boolean.TRUE.equals(isChecked);
            if (binding.switchTopArticles.isChecked() != target) {
                binding.switchTopArticles.setChecked(target);
            }
        });

        viewModel.getNoImageMode().observe(this, isChecked -> {
            boolean target = Boolean.TRUE.equals(isChecked);
            if (binding.switchNoImage.isChecked() != target) {
                binding.switchNoImage.setChecked(target);
            }
        });

        viewModel.getAutoHideBottomBarEnabled().observe(this, enabled -> {
            boolean target = Boolean.TRUE.equals(enabled);
            if (binding.switchAutoHideBottomBar.isChecked() != target) {
                binding.switchAutoHideBottomBar.setChecked(target);
            }
        });

        // 观察操作消息
        viewModel.getOperationMessageRes().observe(this, messageRes -> {
            if (messageRes != null) {
                showShortToast(getString(messageRes));
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

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            if (isChecked) {
                // 打开通知时，检查并请求权限
                if (!NotificationPermissionManager.isGranted(this)) {
                    boolean requested = NotificationPermissionManager.request(this);
                    if (!requested) {
                        // Android 13 以下，直接跳转到系统设置
                        showNotificationPermissionDialog();
                    }
                    // 等待权限结果，暂时不更新状态
                    binding.switchNotifications.setChecked(false);
                } else {
                    viewModel.setNotifications(true);
                }
            } else {
                // 关闭通知，仅更新偏好设置
                viewModel.setNotifications(false);
            }
        });

        binding.switchTopArticles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            viewModel.setHomeTopEnabled(isChecked);
        });

        binding.switchNoImage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            viewModel.setNoImageMode(isChecked);
        });

        binding.switchAutoHideBottomBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            viewModel.setAutoHideBottomBarEnabled(isChecked);
        });

        binding.itemOpenSource.setOnClickListener(v ->
                openWebPage(getString(R.string.settings_open_source_url)));

        binding.btnLogout.setOnClickListener(v -> getDialogController().show(
                new DialogEffect.Confirm(
                        getString(R.string.setting_dialog_logout_title),
                        getString(R.string.setting_dialog_logout_message),
                        getString(R.string.confirm),
                        getString(R.string.cancel),
                        () -> viewModel.logoutWithCallback((success, messageRes) -> {
                            showShortToast(getString(messageRes));
                            if (success) {
                                binding.getRoot().postDelayed(this::finish, 500L);
                            }
                        }),
                        null
                )));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NotificationPermissionManager.REQUEST_CODE_NOTIFICATION) {
            NotificationPermissionManager.handleResult(
                    requestCode, permissions, grantResults,
                    new NotificationPermissionManager.NotificationPermissionCallback() {
                        @Override
                        public void onGranted() {
                            // 权限已授予，更新状态
                            viewModel.setNotifications(true);
                            binding.switchNotifications.setChecked(true);
                            showShortToast(getString(R.string.setting_notification_permission_granted));
                        }

                        @Override
                        public void onDenied() {
                            // 权限被拒绝
                            AppPreferences.setNotificationPermissionDeniedTime(System.currentTimeMillis());

                            if (NotificationPermissionManager.shouldShowRationale(SettingActivity.this)) {
                                // 用户拒绝但未选择"不再询问"，可以再次请求
                                showShortToast(getString(R.string.setting_notification_permission_denied));
                            } else {
                                // 用户选择了"不再询问"，引导到设置页
                                showNotificationPermissionDialog();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从设置页返回时，更新通知权限状态
        updateNotificationSwitchState();
    }

    private void updateNotificationSwitchState() {
        boolean granted = NotificationPermissionManager.isGranted(this);
        boolean prefEnabled = viewModel.getNotifications().getValue() != null
                && viewModel.getNotifications().getValue();

        // 只有权限已授予且偏好设置为开启时，开关才显示为开启
        binding.switchNotifications.setChecked(granted && prefEnabled);
    }

    private void showNotificationPermissionDialog() {
        getDialogController().show(
                new DialogEffect.Confirm(
                        getString(R.string.setting_notification_permission_title),
                        getString(R.string.setting_notification_permission_message),
                        getString(R.string.setting_go_to_settings),
                        getString(R.string.cancel),
                        () -> NotificationPermissionManager.openSettings(this),
                        null
                ));
    }

    private void openWebPage(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            showShortToast(getString(R.string.setting_open_link_failed));
        }
    }

    private void showShortToast(String message) {
        ToastUtils.showShortToast(getDialogController(), message);
    }
}
