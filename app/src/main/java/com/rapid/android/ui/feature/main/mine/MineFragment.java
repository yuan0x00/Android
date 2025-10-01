package com.rapid.android.ui.feature.main.mine;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.core.common.utils.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentMineBinding;
import com.rapid.android.ui.feature.login.LoginActivity;

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
            com.lib.data.session.SimpleSessionManager.SessionState state =
                    com.lib.data.session.SimpleSessionManager.getInstance().getCurrentState();
            if (state == null || !state.isLoggedIn()) {
                navigateToLogin();
            }
            // 如果已登录，则不执行任何操作
        });

        // 主要操作按钮（登录/查看资料）
        binding.btnPrimaryAction.setOnClickListener(v -> {
            com.lib.data.session.SimpleSessionManager.SessionState state =
                    com.lib.data.session.SimpleSessionManager.getInstance().getCurrentState();
            if (state != null && state.isLoggedIn()) {
                ToastUtils.showShortToast("个人中心建设中");
            } else {
                navigateToLogin();
            }
        });

        // 签到按钮
        binding.btnDailyAction.setOnClickListener(v -> {
            // 检查登录状态，只有登录用户才能签到
            com.lib.data.session.SimpleSessionManager.SessionState state =
                    com.lib.data.session.SimpleSessionManager.getInstance().getCurrentState();
            if (state != null && state.isLoggedIn()) {
                viewModel.signIn();
            } else {
                ToastUtils.showShortToast("请先登录");
                navigateToLogin();
            }
        });

        binding.itemSettings.setOnClickListener(v -> openSettings());
        binding.itemFavorites.setOnClickListener(v -> handleProtectedAction("打开收藏"));
        binding.layoutCoin.setOnClickListener(v -> handleProtectedAction("查看积分"));
        binding.layoutFavorite.setOnClickListener(v -> handleProtectedAction("查看收藏"));
        binding.layoutAchievements.setOnClickListener(v -> handleProtectedAction("查看成就"));
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

        // 使用简化的会话管理器
        com.lib.data.session.SimpleSessionManager.getInstance().state.observe(
                getViewLifecycleOwner(),
                sessionState -> {
                    if (sessionState != null) {
                        if (sessionState.isLoggedIn()) {
                            // 已登录，刷新用户信息
                            viewModel.refresh();
                        } else {
                            // 未登录，重置为访客状态
                            viewModel.resetToGuest();
                        }
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
        binding.tvAchievementCount.setText(state.getAchievementDisplay());

        binding.btnDailyAction.setText(state.getDailyActionText());
        binding.btnDailyAction.setEnabled(state.isDailyActionEnabled());

        if (state.isLoggedIn()) {
            binding.btnPrimaryAction.setText("查看资料");
            binding.btnPrimaryAction.setVisibility(View.VISIBLE);
            binding.btnDailyAction.setVisibility(View.VISIBLE); // 签到按钮显示
        } else {
            binding.btnPrimaryAction.setText("立即登录");
            binding.btnPrimaryAction.setVisibility(View.VISIBLE);
            binding.btnDailyAction.setVisibility(View.GONE); // 签到按钮隐藏
        }

        // 根据登录状态设置用户信息卡片的可点击状态和样式
        com.lib.data.session.SimpleSessionManager.SessionState sessionState =
                com.lib.data.session.SimpleSessionManager.getInstance().getCurrentState();
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
        // 通过宿主Activity执行导航
        if (getActivity() instanceof ISettingsNavigator) {
            ((ISettingsNavigator) getActivity()).navigateToSettings();
        } else {
            // Fallback: 提示用户功能不可用
            ToastUtils.showShortToast("功能暂不可用");
        }
    }

    private void handleProtectedAction(String message) {
        com.lib.data.session.SimpleSessionManager.SessionState state =
                com.lib.data.session.SimpleSessionManager.getInstance().getCurrentState();
        if (state != null && state.isLoggedIn()) {
            ToastUtils.showShortToast(message);
        } else {
            ToastUtils.showShortToast("请先登录");
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(getContext(), LoginActivity.class));
    }
}
