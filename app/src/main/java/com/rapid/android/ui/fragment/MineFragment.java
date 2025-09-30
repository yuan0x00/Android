package com.rapid.android.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.core.common.ui.ToastUtils;
import com.core.presentation.activity.BaseFragment;
import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.data.session.AuthSessionManager;
import com.rapid.android.databinding.FragmentMineBinding;
import com.rapid.android.ui.activity.LoginActivity;
import com.rapid.android.ui.activity.SettingActivity;
import com.rapid.android.viewmodel.MineViewModel;

public class MineFragment extends BaseFragment<MineViewModel, FragmentMineBinding> {

    private MineViewModel.MineUiState currentState = MineViewModel.MineUiState.guest();

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
        binding.itemSettings.setOnClickListener(v -> openSettings());
    }

    @Override
    protected void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showShortToast(msg);
            }
        });

        AuthSessionManager.loginState().observe(getViewLifecycleOwner(), loggedIn -> {
            if (Boolean.TRUE.equals(loggedIn)) {
                UserInfoBean cached = AuthSessionManager.userInfo().getValue();
                if (cached != null) {
                    viewModel.applyUserInfo(cached);
                } else {
                    viewModel.refresh();
                }
            } else {
                viewModel.resetToGuest();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void renderState(@NonNull MineViewModel.MineUiState state) {
        currentState = state;

        binding.tvUsername.setText(state.getDisplayName());
    }

    private void openSettings() {
        Intent intent = new Intent(getContext(), SettingActivity.class);
        startActivity(intent);
    }

    private void handleProtectedAction(String message) {
        if (currentState != null && currentState.isLoggedIn()) {
            ToastUtils.showShortToast(message);
        } else {
            ToastUtils.showShortToast("请先登录");
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}
