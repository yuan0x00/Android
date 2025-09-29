package com.rapid.android.presentation.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseActivity;
import com.google.android.material.snackbar.Snackbar;
import com.rapid.android.databinding.ActivitySettingBinding;
import com.rapid.android.presentation.viewmodel.SettingViewModel;

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

        viewModel.getDarkMode().observe(this, isChecked ->
                binding.switchDarkMode.setChecked(isChecked != null ? isChecked : false));

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

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setDarkMode(isChecked));

        binding.switchAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setAutoUpdate(isChecked));

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setNotifications(isChecked));

        binding.btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            finish();
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
