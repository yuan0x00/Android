package com.rapid.android.ui.feature.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.core.ui.presentation.BaseActivity;
import com.google.android.material.snackbar.Snackbar;
import com.rapid.android.databinding.ActivitySettingBinding;
import com.rapid.android.utils.ThemeManager;

public class SettingActivity extends BaseActivity<SettingViewModel, ActivitySettingBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
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

        viewModel.getAutoUpdate().observe(this, isChecked ->
                binding.switchAutoUpdate.setChecked(isChecked != null ? isChecked : false));

        viewModel.getNotifications().observe(this, isChecked ->
                binding.switchNotifications.setChecked(isChecked != null ? isChecked : false));

        // 观察操作消息
        viewModel.getOperationMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                showMessage(message);
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

        binding.switchAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setAutoUpdate(isChecked));

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setNotifications(isChecked));

        binding.btnLogout.setOnClickListener(v -> {
            showConfirmDialog("确认登出", "确定要登出当前账户吗？", (dialog, which) -> {
                viewModel.logoutWithCallback(new SettingViewModel.LogoutCallback() {
                    @Override
                    public void onLogoutComplete(boolean success, String message) {
                        showMessage(message);
                        if (success) {
                            // 登出成功后，延迟一小段时间以确保状态传播完成
                            // 然后结束当前Activity，返回到MainActivity
                            binding.getRoot().postDelayed(() -> finish(), 500); // 延迟500ms
                        }
                    }
                });
            });
        });
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showConfirmDialog(String title, String message,
                                   android.content.DialogInterface.OnClickListener positiveListener) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", positiveListener)
                .setNegativeButton("取消", null)
                .show();
    }

    private void openWebPage(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }
}
