package com.rapid.android.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseFragment;
import com.rapid.android.databinding.FragmentMineBinding;
import com.rapid.android.ui.activity.SettingActivity;
import com.rapid.android.viewmodel.MineViewModel;

public class MineFragment extends BaseFragment<MineViewModel, FragmentMineBinding> {
    @Override
    protected MineViewModel createViewModel() {
        return new ViewModelProvider(this).get(MineViewModel.class);
    }

    @Override
    protected FragmentMineBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        setupClickListeners();
    }

    private void setupClickListeners() {
        // 设置菜单项
        binding.itemSettings.setOnClickListener(v -> openSettings());
    }

    private void openSettings() {
        Intent intent = new Intent(getContext(), SettingActivity.class);
        startActivity(intent);
    }
}