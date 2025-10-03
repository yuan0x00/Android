package com.rapid.android.ui.feature.main.mine;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.core.common.utils.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.R;
import com.rapid.android.databinding.FragmentMineBinding;
import com.rapid.android.ui.feature.login.LoginActivity;
import com.rapid.android.ui.feature.main.mine.coin.CoinActivity;
import com.rapid.android.ui.feature.main.mine.favorite.FavoriteActivity;
import com.rapid.android.ui.feature.setting.SettingActivity;
import com.rapid.android.ui.feature.setting.developer.ProxyConfigActivity;

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
        // 用户信息区域点击事件
        binding.cardProfile.setOnClickListener(v -> {
            // 检查当前登录状态，如果未登录则跳转到登录页面，否则无响应
            com.lib.data.session.SessionManager.SessionState state =
                    com.lib.data.session.SessionManager.getInstance().getCurrentState();
            if (state == null || !state.isLoggedIn()) {
                navigateToLogin();
            }
            // 如果已登录，则不执行任何操作
        });

        // 签到按钮
        binding.btnDailyAction.setOnClickListener(v -> {
            // 检查登录状态，只有登录用户才能签到
            com.lib.data.session.SessionManager.SessionState state =
                    com.lib.data.session.SessionManager.getInstance().getCurrentState();
            if (state != null && state.isLoggedIn()) {
                viewModel.signIn();
            } else {
                ToastUtils.showShortToast(getString(R.string.mine_toast_require_login));
                navigateToLogin();
            }
        });

        binding.itemDeveloper.setOnClickListener(v -> openDeveloperTools());
        binding.itemSettings.setOnClickListener(v -> openSettings());
        binding.layoutCoin.setOnClickListener(v -> {
            com.lib.data.session.SessionManager.SessionState state =
                    com.lib.data.session.SessionManager.getInstance().getCurrentState();
            if (state != null && state.isLoggedIn()) {
                CoinActivity.start(requireContext());
            } else {
                ToastUtils.showShortToast(getString(R.string.mine_toast_require_login));
                navigateToLogin();
            }
        });
        binding.layoutFavorite.setOnClickListener(v -> openFavorites());
    }

    @Override
    protected void setupObservers() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showShortToast(msg);
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // 使用统一的会话管理器
        com.lib.data.session.SessionManager.getInstance().state.observe(
                getViewLifecycleOwner(),
                sessionState -> {
                    if (sessionState != null) {
                        viewModel.applySessionState(sessionState);
                    } else {
                        viewModel.resetToGuest();
                    }
                }
        );
    }


    private void renderState(@NonNull MineViewModel.MineUiState state) {
        currentState = state;

        binding.tvUsername.setText(state.getDisplayName());
        binding.tvTagline.setText(state.getTagline());

        if (state.isShowMembership()) {
            binding.chipMembership.setText(state.getMembershipLabel());
            binding.chipMembership.setVisibility(View.VISIBLE);
        } else {
            binding.chipMembership.setVisibility(View.GONE);
        }

        binding.tvCoinCount.setText(state.getCoinDisplay());
        binding.tvFavoriteCount.setText(state.getFavoriteDisplay());

        binding.btnDailyAction.setText(state.getDailyActionText());
        binding.btnDailyAction.setEnabled(state.isDailyActionEnabled());

        if (state.isLoggedIn()) {
            binding.btnDailyAction.setVisibility(View.VISIBLE); // 签到按钮显示
        } else {
            binding.btnDailyAction.setVisibility(View.GONE); // 签到按钮隐藏
        }

        // 根据登录状态设置用户信息卡片的可点击状态和样式
        com.lib.data.session.SessionManager.SessionState sessionState =
                com.lib.data.session.SessionManager.getInstance().getCurrentState();
        if (sessionState == null || !sessionState.isLoggedIn()) {
            // 未登录时，用户信息卡片需要有点击反馈
            binding.cardProfile.setClickable(true);
            binding.cardProfile.setAlpha(1.0f); // 确保正常显示
        } else {
            // 已登录时，用户信息卡片响应点击但不跳转
            binding.cardProfile.setClickable(true);
            binding.cardProfile.setAlpha(1.0f); // 确保正常显示
        }
    }

    private void openSettings() {
        startActivity(new Intent(getContext(), SettingActivity.class));
    }

    private void openDeveloperTools() {
        startActivity(new Intent(getContext(), ProxyConfigActivity.class));
    }

    private void openFavorites() {
        com.lib.data.session.SessionManager.SessionState state =
                com.lib.data.session.SessionManager.getInstance().getCurrentState();
        if (state != null && state.isLoggedIn()) {
            FavoriteActivity.start(requireContext());
        } else {
            ToastUtils.showShortToast(getString(R.string.mine_toast_require_login));
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(getContext(), LoginActivity.class));
    }
}
