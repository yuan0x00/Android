package com.example.android.ui.fragment.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.lifecycle.ViewModelProvider;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.example.android.databinding.FragmentHomeBinding;
import com.example.android.ui.fragment.home.fragments.recommend.RecommendFragment;
import com.example.android.ui.fragment.home.fragments.topic.TopicFragment;
import com.example.core.base.BaseFragment;
import com.example.core.utils.DisplayUtils;
import com.example.core.utils.SafeAreaUtils;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> implements ITopLayoutHandler {
    private boolean isCollapsed = false;

    @Override
    protected HomeViewModel createViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    protected FragmentHomeBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
    }

    @Override
    protected void initializeViews() {
        SafeAreaUtils.applyTop(binding.getRoot());

        // 设置 ViewPager2 + TabLayout
        setupViewPager();
    }

    private void setupViewPager() {
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(requireActivity());

        adapter.addFragment(new RecommendFragment().setTopLayoutHandler(this), "推荐");
        adapter.addFragment(new TopicFragment(), "话题");

        binding.viewPager.setAdapter(adapter);

        // 将 TabLayout 与 ViewPager2 绑定
        new TabLayoutMediator(binding.topTab, binding.viewPager, (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();
    }

    public void collapseTopLayout() {
        if (isCollapsed) return;

        // 动态获取搜索栏高度
        float topSearchHeight = binding.topSearchLayout.getHeight() > 0 ? binding.topSearchLayout.getHeight() : DisplayUtils.dp2px(60);

        // 使用 ChangeBounds 动画约束变化
        TransitionManager.beginDelayedTransition(
                binding.getRoot(),
                new ChangeBounds()
                        .setDuration(300)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
        );

        // 移动 Guideline - 将Guideline移动到0位置
        binding.guidelineSearchLayoutBottom.setGuidelineBegin(0);

        // 搜索栏淡出 + 上移
        ObjectAnimator searchAlpha = ObjectAnimator.ofFloat(binding.topSearchLayout, "alpha", 1f, 0f);
        ObjectAnimator searchTrans = ObjectAnimator.ofFloat(binding.topSearchLayout, "translationY", 0f, -topSearchHeight);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(searchAlpha, searchTrans);
        animSet.setDuration(300);
        animSet.start();

        isCollapsed = true;
    }

    public void expandTopLayout() {
        if (!isCollapsed) return;

        // 动态获取搜索栏高度
        float topSearchHeight = binding.topSearchLayout.getHeight() > 0 ? binding.topSearchLayout.getHeight() : DisplayUtils.dp2px(60);

        TransitionManager.beginDelayedTransition(
                binding.getRoot(),
                new ChangeBounds()
                        .setDuration(300)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
        );

        // 恢复 Guideline 位置
        binding.guidelineSearchLayoutBottom.setGuidelineBegin((int) topSearchHeight);

        // 恢复搜索栏
        ObjectAnimator searchAlpha = ObjectAnimator.ofFloat(binding.topSearchLayout, "alpha", 0f, 1f);
        ObjectAnimator searchTrans = ObjectAnimator.ofFloat(binding.topSearchLayout, "translationY", -topSearchHeight, 0f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(searchAlpha, searchTrans);
        animSet.setDuration(300);
        animSet.start();

        isCollapsed = false;
    }

    @Override
    public boolean isTopLayoutCollapsed() {
        return isCollapsed;
    }

    @Override
    public void setTopLayoutCollapsed(Boolean isCollapsed) {
        if (isCollapsed) {
            collapseTopLayout();
        } else {
            expandTopLayout();
        }
    }

}