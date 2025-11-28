package com.rapid.android.feature.main.mine;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.BuildConfig;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.core.ui.utils.ToastViewUtils;
import com.rapid.android.databinding.FragmentMineBinding;
import com.rapid.android.feature.developer.DeveloperActivity;
import com.rapid.android.feature.login.LoginActivity;
import com.rapid.android.feature.main.RequiresLoginTab;
import com.rapid.android.feature.main.message.MessageCenterActivity;
import com.rapid.android.feature.main.mine.coin.CoinActivity;
import com.rapid.android.feature.main.mine.favorite.FavoriteActivity;
import com.rapid.android.feature.main.mine.share.ShareActivity;
import com.rapid.android.feature.main.mine.tools.UserToolsActivity;
import com.rapid.android.feature.setting.SettingActivity;
import com.rapid.android.ui.common.LoginHelper;

public class MineFragment extends BaseFragment<MineViewModel, FragmentMineBinding> implements RequiresLoginTab {

    @Override
    protected MineViewModel createViewModel() {
        return new ViewModelProvider(this).get(MineViewModel.class);
    }

    @Override
    protected FragmentMineBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 简化：只需要触发刷新，状态观察由 ViewModel 自动处理
        viewModel.refresh();
    }

    @Override
    protected void initializeViews() {
        setupClickListeners();
        if (!BuildConfig.DEBUG) {
            binding.itemDeveloper.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // 用户信息区域点击事件 - 直接检查当前状态
        binding.cardProfile.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                navigateToLogin();
            }
            // 已登录可以添加打开个人资料等逻辑
        });

        // 其他点击事件保持不变
        binding.btnDailyAction.setOnClickListener(v ->
                LoginHelper.requireLogin(requireContext(), getDialogController(), viewModel::signIn)
        );

        binding.itemNotifications.setOnClickListener(v ->
                LoginHelper.requireLogin(requireContext(), getDialogController(),
                        () -> MessageCenterActivity.start(requireContext()))
        );

        binding.itemDeveloper.setOnClickListener(v -> openDeveloperTools());
        binding.itemTools.setOnClickListener(v ->
                LoginHelper.requireLogin(requireContext(), getDialogController(),
                        () -> UserToolsActivity.start(requireContext()))
        );
        binding.itemSettings.setOnClickListener(v -> openSettings());
        binding.layoutCoin.setOnClickListener(v ->
                LoginHelper.requireLogin(requireContext(), getDialogController(),
                        () -> CoinActivity.start(requireContext()))
        );
        binding.layoutFavorite.setOnClickListener(v ->
                LoginHelper.requireLogin(requireContext(), getDialogController(),
                        () -> FavoriteActivity.start(requireContext()))
        );
        binding.layoutShare.setOnClickListener(v ->
                LoginHelper.requireLogin(requireContext(), getDialogController(),
                        () -> ShareActivity.start(requireContext()))
        );
    }

    @Override
    protected void setupObservers() {
        // 只观察 ViewModel 提供的 UI 状态
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::renderState);
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                showShortToast(msg);
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void renderState(@NonNull MineViewModel.MineUiState state) {
        // 渲染逻辑保持不变
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
        binding.tvShareCount.setText(state.getShareDisplay());

        binding.btnDailyAction.setText(state.getDailyActionText());
        binding.btnDailyAction.setEnabled(state.isDailyActionEnabled());

        if (state.isLoggedIn()) {
            binding.btnDailyAction.setVisibility(View.VISIBLE);
        } else {
            binding.btnDailyAction.setVisibility(View.GONE);
        }

        binding.cardProfile.setClickable(true);
        binding.cardProfile.setAlpha(1.0f);
    }

    private void openSettings() {
        startActivity(new Intent(getContext(), SettingActivity.class));
    }

    private void openDeveloperTools() {
        startActivity(new Intent(getContext(), DeveloperActivity.class));
    }

    private void navigateToLogin() {
        startActivity(new Intent(getContext(), LoginActivity.class));
    }

    private void showShortToast(String message) {
        ToastViewUtils.showShortToast(getDialogController(), message);
    }
}